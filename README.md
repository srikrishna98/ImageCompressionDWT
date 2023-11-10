- Encode and compress a given 512x512 image using Discrete Wavelet Transforms.
- Support for progressive encoding.
- To compile code:

	  javac ImageDisplay.java

- Running:
  java ImageDisplay imagefilePath.rgb n

- args
  - imagefilePath.rgb : relative path to 512x512 image to be encoded. has to be a .rgb file   
  - n : encoded image size (2^n) X (2^n). if n is -1, progressive encoding is applied.

-  Example Usage
   
	  `java ImageDisplay roses_image_512x512.rgb -1`
   
	  `java ImageDisplay roses_image_512x512.rgb 6`
 
