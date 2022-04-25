package bdv.util.benchmark;

import bdv.util.benchmark.RenderingSetup.Renderer;
import bdv.viewer.BasicViewerState;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerState;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import static bdv.util.benchmark.RenderingSetup.createSourceAndConverter;

@State( Scope.Thread )
@Fork( 1 )
public class RenderingSingleSourceBenchmark
{
	public ViewerState state;
	public Renderer renderer;

	@Param({"1", "8"})
	public int numRenderingThreads;

	@Setup
	public void setup()
	{
		final int[] targetSize = { 1680, 997 };
		final AffineTransform3D viewerTransform = new AffineTransform3D();
		viewerTransform.set(
				23.01374483164624, -2.19266950637996, -26.415703464229598, 1275.9089927440664,
				12.592003473417137, 31.68776756743586, 8.340052479934124, -1790.2640725526244,
				23.324635851386212, -14.943467712629362, 21.561163590406633, -1046.864337123486 );


		final Random random = new Random( 1L );

		state = new BasicViewerState();
		final SourceAndConverter< UnsignedByteType > soc = createSourceAndConverter( random, 0, 0, 0 );
		state.addSource( soc );
		state.setSourceActive( soc, true );
		state.setViewerTransform( viewerTransform );

//		final int numRenderingThreads = 1;
//		final int numRenderingThreads = Runtime.getRuntime().availableProcessors();
//		System.out.println( "numRenderingThreads = " + numRenderingThreads );
		renderer = new Renderer( targetSize, numRenderingThreads );
	}

	@Benchmark
	@BenchmarkMode( Mode.AverageTime )
	@OutputTimeUnit( TimeUnit.MILLISECONDS )
	public void bench()
	{
		renderer.render( state );
	}

	public static void main( final String... args ) throws RunnerException, IOException
	{
		final Options opt = new OptionsBuilder()
				.include( RenderingSingleSourceBenchmark.class.getSimpleName() )
				.warmupIterations( 4 )
				.measurementIterations( 8 )
				.warmupTime( TimeValue.milliseconds( 500 ) )
				.measurementTime( TimeValue.milliseconds( 500 ) )
				.build();
		new Runner( opt ).run();

//		final RenderingSingleSourceBenchmark b = new RenderingSingleSourceBenchmark();
//		b.numRenderingThreads = 1;
//		b.setup();
//		b.bench();
//		b.renderer.writeResult( "/Users/pietzsch/Desktop/RenderingSingleSourceBenchmark.png" );
	}
}
