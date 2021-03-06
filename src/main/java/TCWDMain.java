import cyclone.ModelInput;
import cyclone.TropicalCycloneModel;
import cyclone.WilloughbyEqns11;
import org.jblas.FloatMatrix;
import java.util.ArrayList;


public class TCWDMain {

    public static void main(String[] args) {

        ArrayList<Float> bounds = new ArrayList<>();

        ModelInput m = new ModelInput();

        bounds.add(-120.5f);
        bounds.add(5.3f);
        bounds.add(-25.2f);
        bounds.add(65.1f);

        m.setBounds(bounds);
        m.setLongitude(-85.3f);
        m.setLatitude(25.6f);
        m.setNextLongitude(-86.2f);
        m.setNextLatitude(26.2f);
        m.setSpeed(15.0f);
        m.setMaxWindRadius(17.3f);
        m.setMaxWindSpeed(125.3f);

        TropicalCycloneModel tc = new WilloughbyEqns11(0.05f, "out.asc", m);
        tc.runModel();

    }


    public static void testingMatrix() {


        int[] idx = new int[2];
        int Rows = 3;
        int Cols = 5;

        FloatMatrix fm = new FloatMatrix(Rows, Cols);

        fm.put(0, 11);
        fm.put(1, 21);
        fm.put(2, 31);

        fm.put(3, 12);
        fm.put(4, 22);
        fm.put(5, 32);

        fm.put(6, 13);
        fm.put(7, 23);
        fm.put(8, 33);

        fm.put(9, 14);
        fm.put(10, 24);
        fm.put(11, 34);

        fm.put(12, 15);
        fm.put(13, 25);
        fm.put(14, 35);


        for (int i = 0; i < Rows; i++)
            for (int j = 0; j < Cols; j++) {

                System.out.print(" " + fm.get(i + Rows * j) + " ");

                if (j == Cols - 1) System.out.println();
            }

    }
}
