package cyclone;

import org.jblas.FloatMatrix;
import static cyclone.Parameters.*;

public class WilloughbyEqns11 extends TropicalCycloneModel {

    private ModelInput m;

    public WilloughbyEqns11(float cellSize, String FileOuputPath, ModelInput m) {
        super(cellSize, FileOuputPath, m.getBounds());
        this.m = m;
    }

    @Override
    public void runModel() {

        float ye = m.getLatitude();
        float xe = m.getLongitude();

        float cs = m.getSpeed();
        float Mx = m.getMaxWindSpeed();
        float Rx = m.getMaxWindRadius();


        //next
        float nxe = m.getNextLongitude();
        float nye = m.getNextLatitude();

        // position delta values
        float dx = nxe - xe;

        // atan2 handles zero conditions where hurricane doesn't move
        float chi = (float) Math.atan2(Math.sin(dx * PI180) * Math.cos(nye * PI180),
                        Math.cos(ye * PI180) * Math.sin(nye * PI180) - Math.sin(ye * PI180) *
                        Math.cos(nye * PI180) * Math.cos(dx * PI180));

        chi = chi % (2.0f * PI);
        chi = chi * (180f/PI);

        // chi represents hurricane direction relative to longitudinal direction
        if (chi < -90.0f)
            chi = 180f + (-1f) * chi;
        else if (chi < 0.0f)
            chi = 90f + (-1f) * chi;
        else if (chi > 90f)
            chi = chi * (-1f) + 90f;
        else
            chi = 90f - chi;

        System.out.println("chi: "+chi);

        // removing the translation speed component:
        //   Phadke AC, Martino CD, Cheung KF, and Houston SH. 2003. Modeling of
        //   tropical cyclone winds and waves for emergency management. Ocean
        //   Engineering 30(4):553-578.
        //
        // this is not an agreed on value within the hurricane research community
        cs = KNOTS_TO_MPS * cs;
        float Vmax = KNOTS_TO_MPS * Mx - cs*0.5f;

        // convert MWR to kilometers
        float Rmax = Rx * NM_TO_KM;

        // equation 11(a)
        float X1 = 287.6f - 1.942f * Vmax + 7.799f * (float) Math.log(Rmax) + 1.1819f * ye;

        // recommended set at 25.0
        float X2 = 25.0f;

        // equation 11(b)
        float n = 2.134f + 0.0077f * Vmax - 0.4522f * (float) Math.log(Rmax) - 0.0038f * ye;

        // equation 11(c) A >= 0
        float A = 0.5913f + 0.0029f * Vmax - 0.1361f * (float) Math.log(Rmax) - 0.0042f * ye;

        if (A < 0.0) {
            A = 0.0f;
        }

        float w = n * ((1f - A) * X1 + A * X2) / (n * ((1f - A) * X1 + A * X2) + Rmax);

        // newton-raphson routine - inverting equation 2
        float xi_holder = 0.5f;
        int num = 1;
        int MAX_ITERATIONS = 1000;
        float xi = 0.0f;

        for (int i = 0; i < MAX_ITERATIONS; i++) {

            xi = xi_holder - (wf(xi_holder, w) / dwf(xi_holder));

            if (Math.abs(xi - xi_holder) < TOLERANCE)
                break;

            xi_holder = xi;
            num = num + 1;

            if (num == MAX_ITERATIONS)
                System.out.println("WARNING: numerical value of xi did not converge.");

        }

        // "...width of the transition is specified a priori, between 10 and 25 km."
        // pg.1104, paragraph 2
        //
        // calculating radius to start transition
        float R1, R2;
        if (Rmax > 20.0f) {
            R1 = Rmax - 25.0f * xi;
            R2 = R1 + 25.0f;
        } else {
            R1 = Rmax - 10.0f * xi;
            R2 = R1 + 10.0f;
        }

        FloatMatrix grid = new FloatMatrix(longitudes.columns, latitudes.rows);

        for (int i = 0; i < lonSize; i++)
            for (int j = 0; j < latSize; j++) {

                float lat = latitudes.get(i*latSize +  j);
                float lon = longitudes.get(i*latSize + j);

                // The angle, from the eye to a grid point(lon,lat), measure relative to longitudinal direction
               //   K.A. Werley and A.W. McCown. Estimating cyclone wind decay over land. Technical report,
               //   Los Alamos National Laboratory (LANL), Los Alamos, NM (United States), 2007.
                float phi = (float) (180.0f / PI * Math.atan((lat - ye) / (lon - xe) / Math.cos(PI * ((lat + ye) / 2.0f) / 180.0f)));

                if ((lon - xe) < 0.0f)
                    phi = phi + 180f;

                // lat/lon bearing from storm eye
                float theta = phi - chi + 90.0f;

                // approximate distance using Haversine formula
                float range = (float) (Math.pow(Math.sin((lat - ye) * PI180 / 2.0), 2.0) +
                        Math.cos(lat * PI180) * Math.cos(ye * PI180) *
                                Math.pow(Math.sin((lon - xe) * PI180 / 2.0), 2.0));

                range = (float) Math.sqrt(range);
                range = (float) (2f * EARTH_RADIUS * Math.asin(range));

                xi = (range - R1) / (R2 - R1);
                w = wf(xi, 0.0f);

                //ensure 0 <= w <= 1 when inside hurricane eyewall
                if (xi <= 0.0f)
                    w = 0.0f;
                else if (xi >= 1.0f)
                    w = 1.0f;

                float velocity;

                float V0 = (float) (Vmax * ((1.0f - A) * Math.exp(-(range - Rmax) / X1) +
                        A * Math.exp(-(range - Rmax) / X2)));

                if (range <= R1)
                    velocity = (float) ( Vmax * Math.pow((range / Rmax), n));
                else if (range > R1 && range < R2)
                    velocity = (float) ( Vmax * Math.pow((range / Rmax), n) * (1 - w) + V0 * w);
                else
                    velocity = V0;


                // TODO: add land mask features for wind reduction
                // Wind inflow angle
                //   Phadke AC, Martino CD, Cheung KF, and Houston SH. 2003. Modeling of
                //   tropical cyclone winds and waves for emergency management. Ocean
                //   Engineering 30(4):553-578.
                //   equations 11(a-c)

                float beta_angle;
                if (range < Rmax)
                    beta_angle = 10.0f * (1.0f + range / Rmax);
                else if (Rmax <= range && range < 1.2f * Rmax)
                    beta_angle = 20.0f + 25.0f * ((range / Rmax) - 1.0f);
                else
                    beta_angle = 25.0f;

                // Accounting for forward motion of Hurricane - equation 12
                //   Phadke AC, Martino CD, Cheung KF, and Houston SH. 2003. Modeling of
                //   tropical cyclone winds and waves for emergency management. Ocean
                //   Engineering 30(4):553-578.
                //
                // each component <u,v> => <v*cos(), v*sin()>
                // mod function used to ensure degree adjustment is 0<=deg<=360

                float wind_u = (float) (velocity * Math.cos(( (theta + beta_angle) % 360.0f) * PI180));
                float wind_v = (float) (velocity * Math.sin(( (theta + beta_angle) % 360.0f) * PI180));

                // correction factor
                float cf = (2.0f * Rmax * range) / (Rmax * Rmax + range * range);

                // add in correction factor by component
                wind_u = cf * wind_u;
                wind_v = cf * wind_v;

                // asymmetric factor
                float asymf = (float) (cf * cs * Math.cos(theta * PI180) );

                // total wind speed value
                velocity = (float) (Math.sqrt(wind_u * wind_u + wind_v * wind_v)) + asymf;

                grid.put(i + lonSize * j, velocity * MPS_TO_KNOTS);

            }

        System.out.println("writing raster ...");
        writeAsciiGrid(grid);
        System.out.println("Complete! ...");

    }





    private float wf(float x, float val) {

        // powers of x-value
        float x5 = x * x * x * x * x;
        float x6 = x * x * x * x * x * x;
        float x7 = x * x * x * x * x * x * x;
        float x8 = x * x * x * x * x * x * x * x;
        float x9 = x * x * x * x * x * x * x * x * x;

        //solution
        float soln = 126 * x5 - 420 * x6 + 540 * x7 - 315 * x8 + 70 * x9 - val;

        return soln;
    }


    private float dwf(float x) {

        // powers of x-value
        float x4 = x * x * x * x;
        float x5 = x * x * x * x * x;
        float x6 = x * x * x * x * x * x;
        float x7 = x * x * x * x * x * x * x;
        float x8 = x * x * x * x * x * x * x * x;

        //solution
        float soln = 630 * x4 - 2520 * x5 + 3780 * x6 - 2520 * x7 + 630 * x8;


        return soln;
    }

}
