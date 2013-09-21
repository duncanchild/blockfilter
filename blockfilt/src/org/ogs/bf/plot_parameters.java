package org.ogs.bf;
/*

                                 plot_parameters

Generates plot parameters to display graphs in a pannel


File Contains:

  public class plot_parameters

    public plot_parameters( Graphics2D g2, double top_leftx,  double top_lefty,
                                           double bot_rightx, double bot_righty,
                                           double xminval,    double xmaxval, 
                                           double yminval,    double ymaxval )

      top_leftx, top_lefty, bot_rightx, bot_righty define the plot panel size
      xminval, xmaxval, yminval, ymax val          define the data limits that fit into the panel         

    public void plot_axes( Graphics2D g2, String xlabel, String ylabel )  

    public void plot_ticks( Graphics2D g2, double dtick, int nticks, double tickvalmin,
                            double xmin, double xmax, double axisloc, double ymax, char axis )

    public void plot_trace(Graphics2D g2, double [] x, double [] y)

    public void plot_points(Graphics2D g2, double [] x, double [] y, int size)

  class tickpars

    public tickpars( double minval, double maxval )

  ########################## COPYRIGHT NOTICE: ###################################

  Copyright 2013, OpenGeoSolutions, All rights reserved
  Any comments/suggestions for improvement should be made to mdbush@opengeosolutions.com

  Author: M.D. Bush

*/

import java.awt.*;  
import java.awt.font.*;
import javax.swing.*;
import java.io.*;
import java.lang.*;

public class plot_parameters

{

  public double Minx;      // Minimum x in screen coords
  public double Miny;      // Minimum y in screen coords

  public double Maxx;      // Maximum x in screen coords
  public double Maxy;      // Maximum y in screen coords

  public double Dtickx;    // Tick interval x (real units)
  public double Dticky;    // Tick interval y (real units)

  public int Ntickx;       // No. of ticks in x direction
  public int Nticky;       // No. of ticks in x direction

  public double Mintickx;  // Value of Minimum Tick x (real units)
  public double Minticky;  // Value of Minimum Tick y (real units)

  public double Maxtickx;  // Value of Maximum Tick y (real units)
  public double Maxticky;  // Value of Maximum Tick y (real units)

  public double Rangex;    // range of x values (real units)
  public double Rangey;    // range of y values (real units)

  public double Scalex;    // scale x from real to screen units
  public double Scaley;    // scale y from real to screen units

  // Class constructor

  public plot_parameters( Graphics2D g2, double top_leftx,  double top_lefty,
                                         double bot_rightx, double bot_righty,
                                         double xminval,    double xmaxval, 
                                         double yminval,    double ymaxval )

  {

    double centrex = ( top_leftx +  bot_rightx ) / 2.0;
    double centrey = ( top_lefty +  bot_righty ) / 2.0;

    Maxx = centrex + 0.7 * ( bot_rightx - top_leftx ) / 2.0;
    Minx = centrex - 0.7 * ( bot_rightx - top_leftx ) / 2.0;
    Maxy = centrey + 0.5 * ( bot_righty - top_lefty ) / 2.0;
    Miny = centrey - 0.7 * ( bot_righty - top_lefty ) / 2.0;

    tickpars tick_x = new tickpars( xminval, xmaxval );

    Dtickx   = tick_x.dtick;
    Mintickx = tick_x.tickvalmin;
    Maxtickx = tick_x.tickvalmax;
    Ntickx   = tick_x.nticks;

    tickpars tick_y = new tickpars( yminval, ymaxval );

    Dticky   = tick_y.dtick;
    Minticky = tick_y.tickvalmin;
    Maxticky = tick_y.tickvalmax;
    Nticky   = tick_y.nticks;

    Rangex = Maxtickx - Mintickx;   // range of plot in real units
    Rangey = Maxticky - Minticky;

    Scalex = (Maxx - Minx) / Rangex;
    Scaley = (Maxy - Miny) / Rangey;

  }

  // Draw the axes

  public void plot_axes( Graphics2D g2, String xlabel, String ylabel )

  {

    // define axis crossings

    double offsety = ( Maxticky * Minticky > 0.0 )? Minticky: 0.0;  // value of y where y crosses x axis
    double offsetx = ( Maxtickx * Mintickx > 0.0 )? Mintickx: 0.0;  // value of x where x crosses y axis

    double y2xaxis = offsetx - Mintickx;                            // y-axis position in x dirn (screen)
           y2xaxis = Minx + y2xaxis * Scalex;

  
    double x2yaxis = offsety - Minticky;                            // x-axis position in y dirn (screen)
           x2yaxis = Maxy - ( x2yaxis * Scaley );

    plot_ticks( g2, Dtickx, Ntickx, Mintickx, Minx, Maxx, x2yaxis, Maxy, 'x' );
    plot_ticks( g2, Dticky, Nticky, Minticky, Miny, Maxy, y2xaxis, Maxy, 'y' );

    g2.drawLine( (int)Minx,   (int)x2yaxis,    (int)Maxx, (int)x2yaxis ); 
    g2.drawLine( (int)y2xaxis,   (int)Miny, (int)y2xaxis,    (int)Maxy );

    FontRenderContext frc = g2.getFontRenderContext();
    Font font = g2.getFont();

    double str_width  = font.getStringBounds( xlabel, frc ).getWidth();
    double str_height = font.getStringBounds( xlabel, frc ).getHeight();

    g2.drawString(xlabel, (int)(Maxx - str_width), (int)(x2yaxis - str_height));

    str_width  = font.getStringBounds( ylabel, frc ).getWidth();
    str_height = font.getStringBounds( ylabel, frc ).getHeight();

    g2.drawString(ylabel, (int)y2xaxis, (int) (Miny - str_height / 2.0));

  }

  // Draw the axis tick marks

  public void plot_ticks( Graphics2D g2, double dtick, int nticks, double tickvalmin,
                          double xmin, double xmax, double axisloc, double ymax, char axis )

  {

    double nticksm1 = (double)( nticks - 1 );
    double tick_plot = ( xmax - xmin ) / nticksm1;
    double truerange = dtick * nticksm1;
    double power = (double)( (int)( Math.log10( truerange ) ) );
    power = ( power > 0.0 )? Math.pow( 10.0, power ) : Math.pow( 10.0, power - 1.0);
    power /= 2.0;
    power = ( nticks < 10 )? dtick : power;
    int ticklen = 4;

    for( int i = 0; i < nticks; ++i )

    {

      double xval = tickvalmin + (double)i * dtick;
      xval = ( Math.abs( xval ) < 10e-10)? 0.0 : xval;

      int yend = (int)axisloc + ticklen;

      if (axis == 'y')

      {

        String tval = String.format("%8.3g",xval);
        tval.trim();

        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();
        double tval_width  = font.getStringBounds( tval, frc ).getWidth();
        double tval_height = font.getStringBounds( tval, frc ).getHeight();

        double x1 = ymax - ((double)i * tick_plot);

        g2.drawLine( (int)axisloc, (int)x1, (int)yend, (int)x1 ); 
        g2.drawString(tval, yend - 10 - (int)tval_width, (int)( x1 + tval_height / 2.0 ) );
 
      }

      else

      {

        String tval = String.format("%4.0f",xval);
        tval.trim();

        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();
        double tval_width  = font.getStringBounds( tval, frc ).getWidth();
        double tval_height = font.getStringBounds( tval, frc ).getHeight();

        double x1 = xmin + ((double)i * tick_plot);

        g2.drawLine( (int)x1, (int)axisloc, (int)x1, (int)yend );
        g2.drawString(tval, (int)(x1 - tval_width / 2.0), yend + 10 + (int)(tval_height / 2.0) );

      }

    }

  }

  // Draw a trace

  public void plot_trace(Graphics2D g2, double [] x, double [] y)

  {

    int xold = (int)(Minx + ( x[0] - Mintickx ) * Scalex );
    int yold = (int)(Maxy - ( y[0] - Minticky ) * Scaley );

    for(int i = 1; i < x.length; ++i)

    {

      int xnew = (int)(Minx + ( x[i] - Mintickx ) * Scalex );
      int ynew = (int)(Maxy - ( y[i] - Minticky ) * Scaley );

      g2.drawLine( xold, yold, xnew, ynew );

      xold = xnew;
      yold = ynew;

    }

  }

  // Draw a scatter plot of points

  public void plot_points(Graphics2D g2, double [] x, double [] y, int size)

  {

    for(int i = 0; i < x.length; ++i)

    {

 
      int xnew = (int)(Minx + ( x[i] - Mintickx ) * Scalex - (double)size / 2.0 );
      int ynew = (int)(Maxy - ( y[i] - Minticky ) * Scaley - (double)size / 2.0 );
      g2.fillOval( xnew, ynew, size, size);

    }

  }

}

// Class to create tick parameters

class tickpars

{

  public double dtick;
  public double tickvalmin;
  public double tickvalmax;
  public int nticks;

  // Constructor

  public tickpars( double minval, double maxval )

  {


    double drange = maxval - minval;
    double xpower = (double)( (int)Math.log10( drange ) );

    xpower = Math.pow( 10.0, xpower );
    dtick = xpower / 5.0;
    double xrange =  drange + dtick;
    nticks = (int)( xrange / dtick ) + 1;

    tickvalmin = ( minval < 0.0 )? (double)( (int)( minval / dtick ) - 1 ) * dtick
                                 : (double)( (int)( minval / dtick ) ) * dtick;

    tickvalmax = tickvalmin + dtick * (double)nticks;

    double excess = (double)( (int)( (minval - tickvalmin ) / dtick ) );

    tickvalmin = (excess < 0.0)? tickvalmin + excess * dtick : tickvalmin;

    excess = (double)((int)( (tickvalmax - maxval ) / dtick ) );
    tickvalmax = (excess < 0.0)? tickvalmax + excess * dtick : tickvalmax;

    xrange = tickvalmax - tickvalmin;
    nticks = (int)( xrange / dtick + 0.5 ) + 1;

    if( nticks > 20 )

    {

      int ratio1 = (int)( Math.log10( dtick ) );
      double ratio;

      if( ratio1 > 0) { ratio = Math.abs( Math.pow( 10.0, (double)ratio1 ) ); }
      else             { ratio = Math.abs( Math.pow( 10.0, (double)ratio1 + 1.0 ) ); }

      if( ratio  > 1.0 ) { dtick *= ratio; }
      else               { dtick /= ratio; }

      xrange += ( 2.0 * dtick );
      nticks = (int)( xrange / dtick );     

      tickvalmin = ( minval < 0 )? (double)( (int)( minval / dtick ) - 1 ) * dtick
                                 : (double)( (int)( minval / dtick ) ) * dtick; 

    } 

    if( nticks < 5 )

    {

      double ratio1 = (double)( (int)( Math.log10( dtick ) ) );
      double ratio;

      if( ratio1 > -1.0 ) { ratio = Math.abs( Math.pow( 10.0, ratio1 ) ); }
      else                { ratio = Math.abs( Math.pow( 10.0, ratio1 + 1.0 ) ); }

      if( ratio1 > 1.0 ) { dtick /= ratio; }
      else               { dtick *= ratio; }

       xrange += ( 2.0 * dtick );
       nticks = (int)( xrange / dtick );     

      tickvalmin = ( minval < 0 )? (double)( (int)( minval / dtick ) - 1 ) * dtick
                                 : (double)( (int)( minval / dtick ) ) * dtick; 

    }  

    excess = (double)( (int)( (minval - tickvalmin ) / dtick ) );
    tickvalmin = (excess > 0.0)? tickvalmin - excess * dtick : tickvalmin;

    excess = (double)((int)( (tickvalmax - maxval ) / dtick ) );
    tickvalmax = (excess > 0.0)? tickvalmax - excess * dtick : tickvalmax;

    xrange = tickvalmax - tickvalmin;
    nticks = (int)( xrange / dtick + 0.5 ) + 1;

    if (nticks < 5)

    {

      dtick /= 4.0;
      excess = (double)( (int)( (minval - tickvalmin ) / dtick ) );
      tickvalmin = (excess < 0.0)? tickvalmin + excess * dtick : tickvalmin;

      excess = (double)( (int)( ( tickvalmax - maxval ) / dtick ) );
      tickvalmax = (excess < 0.0)? tickvalmax - excess * dtick : tickvalmax;

      xrange = tickvalmax - tickvalmin;
      nticks = (int)( xrange / dtick + 0.5 ) + 1;

    }

  }

}



