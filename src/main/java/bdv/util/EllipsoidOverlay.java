package bdv.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.fitting.ellipsoid.Ellipsoid;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.SphericalToCartesianTransform3D;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.util.LinAlgHelpers;

public class EllipsoidOverlay implements OverlayRenderer
{
	private final AffineTransform3D sourceTransform;

	private Ellipsoid ellipsoid;

	private final ArrayList< RealPoint > normals;

	private Color col;

	private PlaceHolderOverlayInfo info;

	public EllipsoidOverlay()
	{
		sourceTransform = new AffineTransform3D();
		ellipsoid = null;
		normals = new ArrayList< RealPoint >();
		info = null;
	}

	public void setOverlayInfo( final PlaceHolderOverlayInfo info )
	{
		this.info = info;
	}

	public void setEllipsoid( final Ellipsoid ellipsoid )
	{
		this.ellipsoid = ellipsoid;
		updateNormals();
	}

	public void setSourceTransform( final AffineTransform3D t )
	{
		sourceTransform.set( t );
	}

	@Override
	public void setCanvasSize( final int width, final int height )
	{}

	@Override
	public void drawOverlays( final Graphics g )
	{
		if ( ellipsoid == null || info == null || !info.isVisible() )
			return;

		col = new Color( info.getColor().get() );

		final Graphics2D graphics = ( Graphics2D ) g;
		final AffineTransform torig = graphics.getTransform();

		final double sliceDistanceCutoff = 1000;
		final boolean drawEllipsoidProjection = true;
		final boolean drawEllipsoidCoordinateSystem = true;
		final boolean drawEllipsoidSliceIntersection = true;
		final boolean drawNormals = false;

		final double[] lPos = ellipsoid.getCenter();
		final double[] gPos = new double[ 3 ];

		final AffineTransform3D transform = new AffineTransform3D();
		info.getViewerTransform( transform );
		transform.concatenate( sourceTransform );

		transform.apply( lPos, gPos );
		final double z = gPos[ 2 ];
		final double sd = sliceDistance( z, sliceDistanceCutoff );
		if ( sd > -1 && sd < 1 )
		{
			final double[][] S = new double[ 3 ][ 3 ];
			ellipsoid.getCovariance( S );

			final double[][] T = new double[ 3 ][ 3 ];
			for ( int r = 0; r < 3; ++r )
				for ( int c = 0; c < 3; ++c )
					T[ r ][ c ] = transform.get( r, c );

			final double[][] TS = new double[ 3 ][ 3 ];
			LinAlgHelpers.mult( T, S, TS );
			LinAlgHelpers.multABT( TS, T, S );
			// We need make S exactly symmetric or jama eigendecomposition
			// will not return orthogonal V.
			S[ 0 ][ 1 ] = S[ 1 ][ 0 ];
			S[ 0 ][ 2 ] = S[ 2 ][ 0 ];
			S[ 1 ][ 2 ] = S[ 2 ][ 1 ];
			// now S is spot covariance transformed into view coordinates.

			if ( drawEllipsoidProjection )
			{
				graphics.setColor( col );

				final double[][] S2 = new double[ 2 ][ 2 ];
				for ( int r = 0; r < 2; ++r )
					for ( int c = 0; c < 2; ++c )
						S2[ r ][ c ] = S[ r ][ c ];
				final EigenvalueDecomposition eig2 = new Matrix( S2 ).eig();
				final double[] eigVals2 = eig2.getRealEigenvalues();
				final double w = Math.sqrt( eigVals2[ 0 ] );
				final double h = Math.sqrt( eigVals2[ 1 ] );
				final Matrix V2 = eig2.getV();
				final double c = V2.getArray()[ 0 ][ 0 ];
				final double s = V2.getArray()[ 1 ][ 0 ];
				final double theta = Math.atan2( s, c );
				graphics.translate( gPos[ 0 ], gPos[ 1 ] );
				graphics.rotate( theta );
				graphics.draw( new Ellipse2D.Double( -w, -h, 2 * w, 2 * h ) );
				graphics.setTransform( torig );
			}

			if ( drawEllipsoidCoordinateSystem )
			{
				graphics.setColor( col );

				final double[] p0 = ellipsoid.getCenter().clone();
				final double[] p1 = new double[ 3 ];
				final double[] q0 = new double[ 3 ];
				final double[] q1 = new double[ 3 ];
				for ( int i = 0; i < 3; ++i )
				{
					final double[] axis = ellipsoid.getAxes()[ i ].clone();
					LinAlgHelpers.scale( axis, ellipsoid.getRadii()[ i ], p1 );
					LinAlgHelpers.add( p1, p0, p1 );
					transform.apply( p0, q0 );
					transform.apply( p1, q1 );
					graphics.drawLine( ( int ) q0[ 0 ], ( int ) q0[ 1 ], ( int ) q1[ 0 ], ( int ) q1[ 1 ] );
				}
			}

			if ( drawEllipsoidSliceIntersection )
			{
				graphics.setColor( Color.MAGENTA );

				final EigenvalueDecomposition eig = new Matrix( S ).eig();
				final double[] eigVals = eig.getRealEigenvalues();
				final double[][] V = eig.getV().getArray();
				final double[][] D = new double[ 3 ][ 3 ];
				for ( int i = 0; i < 3; ++i )
					D[ i ][ i ] = Math.sqrt( eigVals[ i ] );
				LinAlgHelpers.mult( V, D, T );
				for ( int i = 0; i < 3; ++i )
					D[ i ][ i ] = 1.0 / D[ i ][ i ];
				LinAlgHelpers.multABT( D, V, TS );
				// now T and TS transform from unit sphere to covariance
				// ellipsoid and vice versa

				final double[] vx = new double[ 3 ];
				final double[] vy = new double[ 3 ];
				final double[] vz = new double[ 3 ];
				LinAlgHelpers.getCol( 0, TS, vx );
				LinAlgHelpers.getCol( 1, TS, vy );
				LinAlgHelpers.getCol( 2, TS, vz );

				final double c2 = LinAlgHelpers.squareLength( vx );
				final double c = Math.sqrt( c2 );
				final double a = LinAlgHelpers.dot( vx, vy ) / c;
				final double a2 = a * a;
				final double b2 = LinAlgHelpers.squareLength( vy ) - a2;

				final double[][] AAT = new double[ 2 ][ 2 ];
				AAT[ 0 ][ 0 ] = 1.0 / c2 + a2 / ( b2 * c2 );
				AAT[ 0 ][ 1 ] = -a / ( b2 * c );
				AAT[ 1 ][ 0 ] = AAT[ 0 ][ 1 ];
				AAT[ 1 ][ 1 ] = 1.0 / b2;
				// now AAT is the 2D covariance ellipsoid of transformed unit circle

				final double[] vn = new double[ 3 ];
				LinAlgHelpers.cross( vx, vy, vn );
				LinAlgHelpers.normalize( vn );
				LinAlgHelpers.scale( vz, z, vz );
				final double d = LinAlgHelpers.dot( vn, vz );
				if ( d < 1 )
				{
					final double radius = Math.sqrt( 1.0 - d * d );

					LinAlgHelpers.scale( vn, LinAlgHelpers.dot( vn, vz ), vn );
					LinAlgHelpers.subtract( vz, vn, vz );
					LinAlgHelpers.mult( T, vz, vn );
					final double xshift = vn[ 0 ];
					final double yshift = vn[ 1 ];

					final EigenvalueDecomposition eig2 = new Matrix( AAT ).eig();
					final double[] eigVals2 = eig2.getRealEigenvalues();
					final double w = Math.sqrt( eigVals2[ 0 ] ) * radius;
					final double h = Math.sqrt( eigVals2[ 1 ] ) * radius;
					final Matrix V2 = eig2.getV();
					final double ci = V2.getArray()[ 0 ][ 0 ];
					final double si = V2.getArray()[ 1 ][ 0 ];
					final double theta = Math.atan2( si, ci );

					graphics.translate( gPos[ 0 ] + xshift, gPos[ 1 ] + yshift );
					graphics.rotate( theta );
					graphics.draw( new Ellipse2D.Double( -w, -h, 2 * w, 2 * h ) );
					graphics.setTransform( torig );
				}
			}

			if ( drawNormals  )
			{
				final double[] p0 = new double[ 3 ];
				final double[] p1 = new double[ 3 ];
				final double[] q0 = new double[ 3 ];
				final double[] q1 = new double[ 3 ];

				for ( int i = 0; i < normals.size(); i += 2 )
				{
					normals.get( i ).localize( p0 );
					normals.get( i + 1 ).localize( p1 );
					transform.apply( p0, q0 );
					transform.apply( p1, q1 );
					graphics.setColor( getColor( q0 ) );
					graphics.drawLine( ( int ) q0[ 0 ], ( int ) q0[ 1 ], ( int ) q1[ 0 ], ( int ) q1[ 1 ] );
				}
			}
		}
	}

	private void updateNormals()
	{
		if ( ellipsoid != null )
		{
			normals.clear();

			final double[] r = ellipsoid.getRadii();
			final int biggestRadiusIndex = ( r[ 0 ] > r[ 1 ] ) ?
					( r[ 0 ] > r[ 2 ] ? 0 : 2 ) :
					( r[ 1 ] > r[ 2 ] ? 1 : 2 );
			final double[] L = ellipsoid.getAxes()[ biggestRadiusIndex ].clone();
			final double[] Z = new double[] { 0, 0, 1 };
			final double[] A = new double[ 3 ];
			final double[] tmp = new double[ 3 ];
			LinAlgHelpers.cross( Z, L, tmp );
			LinAlgHelpers.cross( L, tmp, A );
			if ( A[ 2 ] < 0 )
				LinAlgHelpers.scale( A, -1, A );
			LinAlgHelpers.cross( L, A, Z );
			final double[][] cylAxes = new double[][] { A, Z, L };

			final int width = 50;
			final int height = 50;
			final double[] spherical = new double[ 3 ];
			final double[] unit = new double[ 3 ];
			final double[] cyl = new double[ 3 ];
			// point on ellipsoid
			final double[] pe = new double[ 3 ];
			// unit normal at pe
			final double[] ne = new double[ 3 ];

			spherical[ 0 ] = 1.0; // radius
			for ( int ypi = 0; ypi < height; ++ypi )
			{
				spherical[ 1 ] = ypi * Math.PI / height; // inclination
				for ( int xpi = 0; xpi < width; ++xpi )
				{
					spherical[ 2 ] = xpi * 2 * Math.PI / width; // azimuth
					SphericalToCartesianTransform3D.getInstance().apply( spherical, cyl );
					LinAlgHelpers.multT( cylAxes, cyl, unit );
					LinAlgHelpers.normalize( unit );

					LinAlgHelpers.mult( ellipsoid.getPrecision(), unit, ne );
					LinAlgHelpers.scale( unit, Math.sqrt( 1.0 / LinAlgHelpers.dot( unit, ne ) ), pe );
					LinAlgHelpers.mult( ellipsoid.getPrecision(), pe, ne );
					LinAlgHelpers.normalize( ne );
					LinAlgHelpers.add( pe, ellipsoid.getCenter(), pe );

					normals.add( new RealPoint( pe ) );
					for ( int d = 0; d < 3; ++d )
						pe[ d ] += ne[ d ];
					normals.add( new RealPoint( pe ) );
				}
			}
		}
	}

	/** screen pixels [x,y,z] **/
	private Color getColor( final double[] gPos )
	{
		int alpha = 255 - ( int ) Math.round( Math.abs( gPos[ 2 ] ) );

		if ( alpha < 64 )
			alpha = 64;

		return new Color( col.getRed(), col.getGreen(), col.getBlue(), alpha );
	}

	/**
	 * Return signed distance of p to z=0 plane, truncated at cutoff and scaled
	 * by 1/cutoff. A point on the plane has d=0. A Point that is at cutoff or
	 * farther behind the plane has d=1. A point that is at -cutoff or more in
	 * front of the plane has d=-1.
	 */
	private static double sliceDistance( final double z, final double cutoff )
	{
		if ( z > 0 )
			return Math.min( z, cutoff ) / cutoff;
		else
			return Math.max( z, -cutoff ) / cutoff;
	}
}
