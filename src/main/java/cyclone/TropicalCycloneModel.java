package cyclone;

import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jblas.FloatMatrix;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import javax.media.jai.RasterFactory;
import java.awt.*;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public abstract class TropicalCycloneModel {

    protected ArrayList<Float> bbox;
    protected float cellSize;
    protected String outFilePath;

    protected FloatMatrix latitudes;
    protected FloatMatrix longitudes;
    protected int latSize;
    protected int lonSize;

    private CoordinateReferenceSystem crs;
    private final int GRID_WIDTH = 1;
    private final int GRID_HEIGHT = 1;

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

        generateCoordinates();

        System.out.print("");


    }

    private void generateCoordinates(){

        float llx = this.bbox.get(0);
        float lly = this.bbox.get(1);
        float ulx = this.bbox.get(2);
        float uly = this.bbox.get(3);

        float cs = this.cellSize;

        lonSize = getNumberOfCells(cellSize, ulx, llx);
        latSize = getNumberOfCells(cellSize, uly, lly);

        this.latitudes = new FloatMatrix(lonSize,latSize);
        this.longitudes = new FloatMatrix(lonSize,latSize);

        // resample land mask bbox region to given cell size
        GridGeometry2D ggeo = generateBoundingBoxRegion(ulx, llx, uly, lly);
        extractGridValues(lonSize, latSize, ggeo);

    }

    public void extractGridValues(int dx, int dy, GridGeometry2D ggeo){

        // extracting lat lon values for land mask grid points
        for (int i = 0; i < dx; i++) {
            for (int j = 0; j < dy; j++) {

                try {
                    Envelope2D Genv = ggeo.gridToWorld(new GridEnvelope2D(i, j, GRID_WIDTH, GRID_HEIGHT));

                    this.latitudes.put(i+j*dx, (float) Genv.getCenterY() );
                    this.longitudes.put(i+j*dx, (float) Genv.getCenterX() );

                } catch (TransformException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public GridGeometry2D generateBoundingBoxRegion(float ulx, float llx, float uly, float lly){


        // defined new bounding box - an envelope defined by maximum and minimum values.
        BoundingBox newBoundBox = new ReferencedEnvelope(ulx, llx, uly, lly, this.crs);
        GeneralEnvelope cropEnvelope = new GeneralEnvelope(newBoundBox);

        Rectangle rectangle = new Rectangle(lonSize, latSize);
        GridEnvelope genv = new GridEnvelope2D(rectangle);

        return new GridGeometry2D(genv, cropEnvelope);

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

    protected void writeAsciiGrid(FloatMatrix m){

        try {
            FileWriter writer = new FileWriter("output.asc", false);

            writer.write("ncols\t"+lonSize+"\r\n");
            writer.write("nrows\t"+latSize+"\r\n");
            writer.write("xllcorner\t"+bbox.get(0)+"\r\n");
            writer.write("yllcorner\t"+bbox.get(1)+"\r\n");
            writer.write("cellsize\t"+cellSize+"\r\n");
            writer.write("NODATA_value\t -9999 \r\n");



            for(int i=0; i<lonSize; i++){
                for(int j=0; j<latSize; j++){
                    writer.write(m.get(i+j*lonSize)+" ");
                }
                writer.write("\r\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public abstract void runModel();




}
