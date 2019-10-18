package bdv.util;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;

import static net.imglib2.cache.img.DiskCachedCellImgOptions.options;

public class ExampleMultiresolutionSource {

    public static void main( String[] args )
    {
        System.setProperty( "apple.laf.useScreenMenuBar", "true" );

        // Make edge display on demand
        final int[] cellDimensions = new int[] { 32, 32, 32 };

        // Cached Image Factory Options
        final DiskCachedCellImgOptions factoryOptions = options()
                .cellDimensions( cellDimensions )
                .cacheType( DiskCachedCellImgOptions.CacheType.BOUNDED )
                .maxCacheSize( 100 );

        // Creates cached image factory of Type Byte
        final DiskCachedCellImgFactory<ARGBType> factory = new DiskCachedCellImgFactory<>( new ARGBType(), factoryOptions );

        Img<ARGBType>[] multiResolutionImgs = new Img[5];

        long[] imageDimensions = new long[]{1000,1000,1000};

        multiResolutionImgs[0] = getHardToComputeImage(factory,imageDimensions,1);
        multiResolutionImgs[1] = getHardToComputeImage(factory,imageDimensions,2);
        multiResolutionImgs[2] = getHardToComputeImage(factory,imageDimensions,4);
        multiResolutionImgs[3] = getHardToComputeImage(factory,imageDimensions,8);
        multiResolutionImgs[4] = getHardToComputeImage(factory,imageDimensions,16);


        AbstractSource as = new AbstractSource<ARGBType>(new ARGBType(), "source_zero") {
            @Override
            public RandomAccessibleInterval getSource(int t, int level) {

                return multiResolutionImgs[level];
            }

            @Override
            public void getSourceTransform(int t, int level, AffineTransform3D transform) {
                AffineTransform3D at = new AffineTransform3D();
                at.scale(Math.pow(2,level));
                transform.set(at);
            }

            @Override
            public int getNumMipmapLevels()
            {
                return 5;
            }

        };

        BdvFunctions.show(as);
    }

    static Img<ARGBType> getHardToComputeImage(DiskCachedCellImgFactory<ARGBType> factory, long[] fullResolutionDimensions, double downscale) {

        // Creates border image, with cell Consumer method, which creates the image
        final Img<ARGBType> hardToComputeImage = factory.create( new long[]{(long)(fullResolutionDimensions[0]/downscale),
                                                                                     (long)(fullResolutionDimensions[1]/downscale),
                                                                                     (long)(fullResolutionDimensions[2]/downscale)}, cell -> {
            // Cursor on output image
            final Cursor<ARGBType> out = cell.localizingCursor();

            Thread.sleep(10);
            // Loops through voxels
            while ( out.hasNext() ) {
                out.next().set( ARGBType.rgba(out.getIntPosition(0),out.getIntPosition(1),out.getIntPosition(2),255 ));
            }

        }, options().initializeCellsAsDirty( true ) );

        return hardToComputeImage;
    }



}
