Standard Java FX application

To run a scene, specify .simp file name in the program arguments. I included the jar in case there are compilation problems.

In general, all the features should work, and I provided some test scenes to showcase them:
	test_scene1.simp
	test_scene2.simp
	test_scene3.simp

The only issue I know of is that polygons which are partially outside the viewing frustum are not clipped before rasterization.
Consequently, if the camera is setup inside an object, it will take a while to render. If you run a scene and it is taking a while to render, 
try moving the camera back or increase the size of the viewing plane.