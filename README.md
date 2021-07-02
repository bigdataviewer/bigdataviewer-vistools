[![](https://github.com/bigdataviewer/bigdataviewer-vistools/actions/workflows/build-main.yml/badge.svg)](https://github.com/bigdataviewer/bigdataviewer-vistools/actions/workflows/build-main.yml)

# bigdataviewer-vistools
Helpers to use BigDataViewer as a quick visualization tool in other projects.
This is intented to replace/augment `ImageJFunction.show()` from [imglib2-ij](https://github.com/imglib/imglib2-ij).

Please note, that this is still in early stages of development.
Documentation is sparse.
API will break frequently in the foreseeable future.

## Usage

```
Random random = new Random();
Img<ARGBType> img = ArrayImgs.argbs(100, 100, 100);
img.forEach(t -> t.set(random.nextInt()));
Bdv bdv = BdvFunctions.show(img, "img");
```
creates a random 3D ARGB img and shows it in a BDV window.

All `BdvFunctions` methods will return some instance of `Bdv` which can be used to close the BDV window:
```
bdv.close()
```
or add more stuff to the same window:
```
BdvFunctions.show(img2, "img2", Bdv.options().addTo(bdv));
```
Via `bdv.getBdvHandle()` you can get access to the BDV `ViewerPanel` and `SetupAssignments` allowing more fine-grained
manipulations of BDV state.

The concrete `Bdv` instance returned from `BdvFunctions.show(img, "img")` in the above example is `BdvStackSource<ARGBType>` and
has additional methods, e.g., `removeFromBdv()` which removes `img` from the BDV window.

To display a 2D image, display a 3D image as a stack of 2D slices over time etc:
```
BdvFunctions.show(img2, "img2", Bdv.options().is2D());
```
The `is2D()` option is per Viewer window, not per stack. If it is set, the BDV navigation mouse and keybindings are set up for 2D, etc...

More fine-grained control can be achieved with `axisOrder()` option:
```
BdvFunctions.show(img2, "img2", Bdv.options().is2D().axisOrder(XYT))
```
`AxisOrder` specifies how the stack dimensions are interpreted.
For BDV with 3D navigation, the follwoing are useful: `XYZ, XYZC, XYZT, XYZCT, XYZTC`.
For BDV with 2D navigation, the following are useful: `XY, XYC, XYT, XYCT, XYTC`.
You should interpret `C` and `T` losely. The effect is that `T` will be mapped to the time slider of the BDV,
while `C` is mapped to BDV sources.
(This also means that you should not have images with a large `C` dimension.)

There is/will be stuff to add annotation overlays. Currently `BdvFunctions.showPoints()` is available.

Here is an example screenshot where several 3D stacks, a set of boundary points, and 3D ellipsoids fitted to the boundary points were added to a BDV window. It also shows how the usual BDV dialogs can be used to adjust visibility / brightness / colors of tbe individual sources.
![screenshot](screenshot.png)
