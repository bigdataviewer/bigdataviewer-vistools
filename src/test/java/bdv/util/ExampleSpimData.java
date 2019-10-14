package bdv.util;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;

public class ExampleSpimData
{
	public static void main( final String[] args ) throws SpimDataException
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		DatasetHelper.getDataset(DatasetHelper.BDV_HisYFP_SPIM_H5); // Download hdf5 - > cached on hard drive
		DatasetHelper.getDataset(DatasetHelper.BDV_Drosophila_H5); // Download hdf5 - > cached on hard drive
		final String xmlFilename = DatasetHelper.getDataset(DatasetHelper.BDV_HisYFP_SPIM_XML).getAbsolutePath();
		final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
		BdvFunctions.show( spimData );
	}
}
