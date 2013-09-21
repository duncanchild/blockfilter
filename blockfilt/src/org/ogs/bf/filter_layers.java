package org.ogs.bf;
/*

                       filter_layers

  Java main class for calculating and displaying the analytical and
  windowed DFT spectra after application of an Ormsby filterof on a 
  series of blocks having different acoustic impedances. The filtered 
  block trace is also displayed along with the reflection coefficients
  at each interface together with with the filtered reflection coeffient trace.
  The analytical spectrum of the reflection coefficient series is also 
  displayed along with the associated windowed DFT spectrum.

  If spikes (zero length blocks) are detected the reflection coefficients and
  their associated spectra and traces are set to zero.


  Requires:

    Blockfilter
    Help
    parameter_box
    plot_parameters
    utilities

   Resource files:

     help_filter_layers.html
     about_filter_layers.html

  ########################## COPYRIGHT NOTICE: ###################################

  Copyright 2013, OpenGeoSolutions, All rights reserved
  Any comments/suggestions for improvement should be made to mdbush@opengeosolutions.com

  Author: M.D. Bush

  Original Sep. 2013 based on Perl Module 'BlockFilter.pm' by M.D. Bush, 2003

*/

import java.io.*;

public class filter_layers 

{

  public static void main(String[] args) throws IOException

  {

    parameter_box input_boxs = new parameter_box();

  }

}



