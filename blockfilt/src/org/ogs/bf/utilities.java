package org.ogs.bf;
/*

                                 utilities

A class of utility methods


File Contains:

  public class utilities

  Contains:

    public utilities())  Class constructor for utilities

    public void showerr( JPanel pane, String str )

    public double maxval( double [] x )

    public double minval( double [] x )

    public boolean isNumeric(String str)

    public void wrt2xgraph ( PrintWriter pw, String name, double [] xvals, double [] yvals )

  ########################## COPYRIGHT NOTICE: ###################################

  Copyright 2013, OpenGeoSolutions, All rights reserved
  Any comments/suggestions for improvement should be made to mdbush@opengeosolutions.com

  Author: M.D. Bush

*/


import java.io.*;
import javax.swing.*;
import java.awt.*;

public class utilities

{

  // Class Constructor

  public utilities() { }

  // Display an error message with a warning sound

  public void showerr( JPanel pane, String str )

  {

   java.awt.Toolkit.getDefaultToolkit().beep();
   JOptionPane.showMessageDialog(pane,str);

  }

  // Find the maximum value of a vector

  public double maxval( double [] x )

  {

    double max = x[0];

    for(int i = 1; i < x.length; ++i) { max = ( max > x[i] )? max : x[i]; }

    return max;

  }

  // Find the minimum value of a vector

  public double minval( double [] x )

  {

    double min = x[0];

    for(int i = 1; i < x.length; ++i) { min = ( min < x[i] )? min : x[i]; }

    return min;

  }

  // Test to see if a string repesents a number

  public boolean isNumeric(String str)

  {

    return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.

  }

  // Write out a trace in 'xgraph' format

  public void wrt2xgraph ( PrintWriter pw, String name, double [] xvals, double [] yvals )

  {

    pw.printf("\"%s\"\n", name);

    for( int i = 0; i < xvals.length; ++i ) { pw.printf("%g  %g\n",xvals[i], yvals[i]); }

    pw.println("");

  }

}
