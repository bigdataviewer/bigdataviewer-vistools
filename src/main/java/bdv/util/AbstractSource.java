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

import java.util.function.Supplier;

import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.Views;

public abstract class AbstractSource< T extends NumericType< T > > implements Source< T >
{
	protected final T type;

	protected final static int numInterpolationMethods = 2;

	protected final static int iNearestNeighborMethod = 0;

	protected final static int iNLinearMethod = 1;

	protected final String name;

	@SuppressWarnings( "unchecked" )
	protected final InterpolatorFactory< T, RandomAccessible< T > >[] interpolatorFactories = new InterpolatorFactory[ numInterpolationMethods ];
	{
		interpolatorFactories[ iNearestNeighborMethod ] = new NearestNeighborInterpolatorFactory<>();
		interpolatorFactories[ iNLinearMethod ] = new ClampingNLinearInterpolatorFactory<>();
	}

	public AbstractSource( final T type, final String name )
	{
		this.type = type;
		this.name = name;
	}

	public AbstractSource( final Supplier< T > typeSupplier, final String name )
	{
		this( typeSupplier.get(), name );
	}

	@Override
	public boolean isPresent( final int t )
	{
		return true;
	}

	@Override
	public T getType()
	{
		return type;
	}

	@Override
	public RealRandomAccessible< T > getInterpolatedSource( final int t, final int level, final Interpolation method )
	{
		return Views.interpolate( Views.extendZero( getSource( t, level ) ), interpolatorFactories[ method == Interpolation.NLINEAR ? iNLinearMethod : iNearestNeighborMethod ] );
	}

	@Override
	public String getName()
	{
		return name;
	}
}
