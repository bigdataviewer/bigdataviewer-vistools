package bdv.util;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;

public class ExampleSpimData
{
	public static void main( final String[] args ) throws SpimDataException
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		final String xmlFilename = "/Users/pietzsch/workspace/data/111010_weber_full.xml";
		final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
		BdvFunctions.show( spimData );
	}
}
