#version 150

//#####################################################################
//                          Piecewise interpolation
//#####################################################################

// This function returns a linearily interpolated value between a and b depending on x.
// x is the input, a is the start value, b is the end value
float linearPiecewiseInterpolation(float x, float a, float b) {
    if (x <= a) {
        return 0.0;
    }
    if (x >= b) {
        return 1.0;
    }

    return (x - a) / (b - a);
}

// This function returns an exponentially interpolated value between a and b depending on x.
// x is the input, a is the start value, b is the end value, k adjusts the rate
// k CANNOT be less than 0 or equal to 1. Use linear function instead of k = 1.
float exponentialPiecewiseInterpolation(float x, float a, float b, float k) {
    if (x <= a) {
        return 0.0;
    }
    if (x >= b) {
        return 1.0;
    }

    float t = (x - a) / (b - a);
    return (pow(k, t) - 1) / (k - 1);
}