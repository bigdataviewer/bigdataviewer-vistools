/*
 * #%L
 * BigDataViewer core classes with minimal dependencies
 * %%
 * Copyright (C) 2012 - 2015 BigDataViewer authors
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bdv.util;

import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.Views;

public class RandomAccessibleSource4D< T extends NumericType< T > > implements Source< T >
{
	private final T type;

	private final RandomAccessible< T > source;

	private final Interval interval;

	private final Interval timeSliceInterval;

	protected int currentTimePointIndex;

	private RandomAccessible< T > currentSource;

	private final RealRandomAccessible< T >[] currentInterpolatedSources;

	private final AffineTransform3D sourceTransform;

	private final String name;

	private final VoxelDimensions voxelDimensions;

	private final static int numInterpolationMethods = 2;

	private final static int iNearestNeighborMethod = 0;

	private final static int iNLinearMethod = 1;

	private final InterpolatorFactory< T, RandomAccessible< T > >[] interpolatorFactories;

	public RandomAccessibleSource4D(
			final RandomAccessible< T > img,
			final Interval interval,
			final T type,
			final String name )
	{
		this( img, interval, type, new AffineTransform3D(), name );
	}

	public RandomAccessibleSource4D(
			final RandomAccessible< T > img,
			final Interval interval,
			final T type,
			final AffineTransform3D sourceTransform,
			final String name )
	{
		this.source = img;
		this.interval = interval;
		this.timeSliceInterval = new FinalInterval( Views.hyperSlice( Views.interval( source, interval ), 3, 0 ) );
		this.name = name;
		this.type = type;
		this.sourceTransform = sourceTransform;
		voxelDimensions = null; // TODO?

		interpolatorFactories = new InterpolatorFactory[ numInterpolationMethods ];
		interpolatorFactories[ iNearestNeighborMethod ] = new NearestNeighborInterpolatorFactory< >();
		interpolatorFactories[ iNLinearMethod ] = new ClampingNLinearInterpolatorFactory< >();

		currentInterpolatedSources = new RealRandomAccessible[ numInterpolationMethods ];
		loadTimepoint( 0 );
	}

	private void loadTimepoint( final int timepointIndex )
	{
		currentTimePointIndex = timepointIndex;
		if ( isPresent( timepointIndex ) )
		{
			final T zero = getType().createVariable();
			zero.setZero();
			currentSource = Views.hyperSlice( source, 3, timepointIndex );
//			ImageJFunctions.show( currentSource );
			for ( int method = 0; method < numInterpolationMethods; ++method )
				currentInterpolatedSources[ method ] = Views.interpolate( currentSource, interpolatorFactories[ method ] );
		}
		else
		{
			currentSource = null;
			for ( int method = 0; method < numInterpolationMethods; ++method )
				currentInterpolatedSources[ method ] = null;
		}
	}

	@Override
	public boolean isPresent( final int t )
	{
		return interval.min( 3 ) <= t && t <= interval.max( 3 );
	}

	@Override
	public T getType()
	{
		return type;
	}

	@Override
	public RandomAccessibleInterval< T > getSource( final int t, final int level )
	{
		if ( t != currentTimePointIndex )
			loadTimepoint( t );
		return Views.interval( currentSource, timeSliceInterval );
	}

	@Override
	public RealRandomAccessible< T > getInterpolatedSource( final int t, final int level, final Interpolation method )
	{
		if ( t != currentTimePointIndex )
			loadTimepoint( t );
		return currentInterpolatedSources[ method == Interpolation.NLINEAR ? iNLinearMethod : iNearestNeighborMethod ];
	}

	@Override
	public synchronized void getSourceTransform( final int t, final int level, final AffineTransform3D transform )
	{
		transform.set( sourceTransform );
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public VoxelDimensions getVoxelDimensions()
	{
		return voxelDimensions;
	}

	@Override
	public int getNumMipmapLevels()
	{
		return 1;
	}
}
