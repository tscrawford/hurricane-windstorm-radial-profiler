package cyclone;

import org.jblas.DoubleMatrix;

import javax.media.jai.RasterFactory;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

public abstract class TropicalCycloneModel {

    protected ArrayList<Float> bbox;
    protected float cellSize;
    protected String outFilePath;

    protected float[] lats;
    protected float[] lons;

    protected DoubleMatrix range;


    TropicalCycloneModel(float cellSize, String outputFile, ArrayList<Float> bounds){

        // calc matrix dimensions based on bounding-box and cell size values
        // lower-left (lon, lat) to upper right (lon, lat)

        if (bounds.size() == 4) {
            this.bbox = bounds;
        } else {
            System.out.println(" -- WARNING: bound box size if NOT four! ");
        }

        this.cellSize = cellSize;
        this.outFilePath = outputFile;


    }

    private void generateCoordinates(){

        float llx = this.bbox.get(0);
        float lly = this.bbox.get(1);
        float ulx = this.bbox.get(2);
        float uly = this.bbox.get(3);

        float cs = this.cellSize;

        int dx = getNumberOfCells(ulx, llx, cellSize);
        int dy = getNumberOfCells(uly, lly, cellSize);




    }

    /**
     * Approximates the number of grid cells between bounding coordinate values, assuming
     * a structured grid.
     *
     * @param cs cell size in decimal degrees
     * @param u upper right coordinate value decimal degrees
     * @param l lower left coordinate value decimal degrees
     * @return number of grid cells between the upper right and lower left coordinate
     */
    public int getNumberOfCells(double cs, double u, double l){
        return (int) Math.abs((u - l) / cs);
    }

    /**
     * NOT USED - scratch function...
     * @param rows
     * @param cols
     * @param cellSize
     */
    protected void generateRaster(int rows, int cols, float cellSize){

        final int width  = 500;
        final int height = 500;

        WritableRaster raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT,
                width, height, 1, null);

        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                raster.setSample(x, y, 0, x+y);
            }
        }


    }


    protected void writeRaster(){

    }


}
