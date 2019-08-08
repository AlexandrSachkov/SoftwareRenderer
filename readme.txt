Standard Java FX application

To run a scene, specify .simp file name in the program arguments. I included the jar in case there are compilation problems.

Personal test scenes displaying shading on a cube and an .obj model:
	cube_shading_test.simp
	flat_test.simp
	gouraud_test.simp
	phong_test.simp

Two known issues:

1) Polygons which are partially outside the viewing frustum are not clipped before rasterization.
Consequently, if the camera is setup inside an object, it will take a while to render. If you run a scene and it is taking a while to render, 
try moving the camera back or increasing the size of the viewing plane.

2) Camera does not taking account the aspect ration when scaling to display size (images will turn out stretched if the display area width does not equal its height)

There was a depth buffer bug present in the last assignment which I only discovered after running the provided test files where maximum depth would always be set to 100
 instead of the far clipping plane depth. It was fixed for this assignment.

I am submitting 2 days late and would like to use my grace days to cover it (have 3 left).