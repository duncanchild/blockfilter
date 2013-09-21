package org.ogs.bf;
/*

                                 parameter_box

Creates the input parameter and execution box for 'filter_layers'.

Resource files used:

  help_filter_layers.html
  about_filter_layers.html

File Contains:

  public class public parameter_box

  Contains:

    public parameter_box(  )  Class constructor for parameter_box

    public void show_help( int choice)  Show selected help html files

    private void create_image ( Component image_frame)

    private void read_model_file()

    private void save_model_file()

    private void save_blktrc_file()

    private void save_rctrc_file()

    private void save_blkspec_file()

    private void save_rcspec_file()

    void show_spec_window ( Graphics2D g2, plot_parameters plot_pars, Color c )

    void models2plot ( double [] intimes, double [] impedances, double [] outtimes, 
                       double [] times, double [] model, String type )


  class block_plot extends JPanel

  Contains:

    protected void paintComponent(Graphics g)

  class rc_plot extends JPanel

  Contains:

    protected void paintComponent(Graphics g)

  class blk_spectra_plot extends JPanel

  Contains:

    protected void paintComponent(Graphics g)

  class rc_spectra_plot extends JPanel

  Contains:

    protected void paintComponent(Graphics g)

  class Gridbagin extends JPanel implements ActionListener

  Contains:

    public Gridbagin ()

    void addGB( Component component, int x, int y) Adds a component to a gridbag layout

    public void actionPerformed( ActionEvent e ) Mouse click detector

    public void load_model (String str)

    public int validate_model (String str)

  Requires:

    Blockfilter
    Help
    plot_parameters
    utilities

  ########################## COPYRIGHT NOTICE: ###################################

  Copyright 2013, OpenGeoSolutions, All rights reserved
  Any comments/suggestions for improvement should be made to mdbush@opengeosolutions.com

  Author: M.D. Bush

*/

import java.io.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.image.*;
import java.util.*;
import java.text.DateFormat;

import javax.imageio.ImageIO;

import java.util.regex.*;

import javax.swing.filechooser.FileNameExtensionFilter;

public class parameter_box extends JFrame 

{

  public double [] impedances;
  public double [] intimes;

  public double [] filter;
  public double dsamp;
  public double UnitSc;
  public double taper_percent;
  public double dft_window_top;
  public double dft_window_bot;
  public boolean calculate;

  public final Gridbagin upper;
  public final JPanel lower;
  public final JTextArea blk_model;
  public final JFrame plot_frame;

  public final block_plot blk_trace;
  public final rc_plot rc_trace;
  public final blk_spectra_plot blk_spectra;
  public final rc_spectra_plot rc_spectra;

  public Image graph_display = null;

  public Blockfilter blk_filter = null;
  public String pwd = (String)(System.getProperty("user.dir"));

  public utilities utils;

  public parameter_box ()

  {

    utils = new utilities();

    calculate = false;

    // Set up GUI for input

    JFrame frame = new JFrame( "OGS - Blockfilter" );

    // Create the file menu

    JMenu file = new JMenu( "File" );
    JMenu help = new JMenu( "Help" );

    // Define load data item on file menu

    JMenuItem loadItem = new JMenuItem( "Load Model Values" );
    loadItem.setMnemonic( KeyEvent.VK_O );
    loadItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_O, Event.CTRL_MASK ) );
    loadItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { read_model_file(); } } );
    file.add(loadItem);

    // Define save model times /impedances item on file menu

    JMenuItem saveMItem = new JMenuItem( "Save Model Values" );
    saveMItem.setMnemonic( KeyEvent.VK_M );
    saveMItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_M, Event.CTRL_MASK ) );
    saveMItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { save_model_file(); } } );
    file.add(saveMItem);

    // Define save filtered block traces item on file menu

    JMenuItem saveTItem = new JMenuItem( "Save Block Traces" );
    saveTItem.setMnemonic( KeyEvent.VK_T );
    saveTItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_T, Event.CTRL_MASK ) );
    saveTItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { save_blktrc_file(); } } );
    file.add(saveTItem);

    // Define save RC data item on file menu

    JMenuItem saveRItem = new JMenuItem( "Save RC Traces" );
    saveRItem.setMnemonic( KeyEvent.VK_R );
    saveRItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_R, Event.CTRL_MASK ) );
    saveRItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {save_rctrc_file(); } } );
    file.add(saveRItem);

    // Define save block spectra data item on file menu

    JMenuItem saveSItem = new JMenuItem( "Save Block Spectra" );
    saveSItem.setMnemonic( KeyEvent.VK_S );
    saveSItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S, Event.CTRL_MASK ) );
    saveSItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { save_blkspec_file(); } } );
    file.add(saveSItem);

    // Define save block spectra data item on file menu

    JMenuItem saveFItem = new JMenuItem( "Save RC Spectra" );
    saveFItem.setMnemonic( KeyEvent.VK_F );
    saveFItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F, Event.CTRL_MASK ) );
    saveFItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { save_rcspec_file(); } } );
    file.add(saveFItem);

    // Define save block spectra data item on file menu

    JMenuItem saveIItem = new JMenuItem( "Save Display Image" );
    saveIItem.setMnemonic( KeyEvent.VK_I );
    saveIItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_I, Event.CTRL_MASK ) );
    saveIItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { create_image(plot_frame); } } );
    file.add(saveIItem);

    // Define quit item on file menu

    JMenuItem quitItem = new JMenuItem( "Quit" );
    quitItem.setMnemonic( KeyEvent.VK_Q );
    quitItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Q, Event.CTRL_MASK ) );
    quitItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { System.exit(0); } } );
    file.add(quitItem);

    // Define help item on help menu

    JMenuItem helpItem = new JMenuItem( "Detailed Help" );
    helpItem.setMnemonic( KeyEvent.VK_H );
    helpItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_H, Event.CTRL_MASK ) );
    helpItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { show_help( 0 ); } } );
    help.add(helpItem);

    // Define about item on file menu

    JMenuItem aboutItem = new JMenuItem( "About" );
    aboutItem.setMnemonic( KeyEvent.VK_A );
    aboutItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_A, Event.CTRL_MASK ) );
    aboutItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { show_help( 1 ); } } );
    help.add(aboutItem);

    // Create the menu bar add items then add the meu bar to the JFrame

    JMenuBar menubar = new JMenuBar();
    menubar.add( file );
    menubar.add(Box.createHorizontalGlue());
    menubar.add( help );
    frame.setJMenuBar(menubar);

    // Set up Frame

    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setSize(300,500);
    frame.setLocation(10,50);
    frame.setLayout(new GridLayout(2,1));

    // Create and initialize two panels and add them into the frame

    upper = new Gridbagin();
    lower = new JPanel();

    upper.setSize(250,300);
    lower.setSize(200,300);
    lower.setLayout(new GridLayout(1,1));
    frame.add(upper);
    frame.add(lower);

    // Create an editable text area with a scrollbar and add it to the lower panel

    blk_model = new JTextArea();

    blk_model.setLocation(0,300);
    blk_model.setText("");
    blk_model.setFont( new Font("Serif", Font.BOLD, 12) );
    lower.add(new JScrollPane(blk_model), BorderLayout.CENTER);
      
    frame.setVisible(true);

    // Create a new frame for display of the graphs

    plot_frame = new JFrame( "OGS - Blockfilter traces and spectra" );

    plot_frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    plot_frame.setSize(1100,600);
    plot_frame.setLocation(150,300);
    plot_frame.setLayout(new GridLayout(2,2));

    // Create the plots to be added to the display

    blk_trace   = new block_plot();
    rc_trace    = new rc_plot();
    blk_spectra = new blk_spectra_plot();
    rc_spectra  = new rc_spectra_plot();

    // Add the plots to the display

    plot_frame.add(blk_trace);
    plot_frame.add(blk_spectra);
    plot_frame.add(rc_trace);
    plot_frame.add(rc_spectra);

    plot_frame.setVisible(true);

  }

  // Callback for a help selection

  public void show_help( int choice) { Help help_show = new Help( choice ); }

  // Callback to save the display as an image

  private void create_image ( Component image_frame)

  {

    if( blk_filter != null )

    {

      // Display the file dialog showing only jpg and gif file extensions

      JFileChooser chooser = new JFileChooser ( new File(pwd) );
      FileNameExtensionFilter filter = new FileNameExtensionFilter( "JPG & GIF Images", "jpg", "gif");
      chooser.setFileFilter(filter);
      chooser.setDialogTitle("Save display as image");

      int result = chooser.showSaveDialog(this);

      if (result == JFileChooser.CANCEL_OPTION) return;

      // Save the display as an image file

      try

      {

        File file = chooser.getSelectedFile();

        String name = file.getName();
        String newname = null;

        int namelen = name.length();

        // Define the available file types

        String [] type = { "gif", "jpg", "png"};

        // Define the pattern match

        String [] pattern = { "(?i)\\.gif", "(?i)\\.jpg", "(?i)\\.png"};

        // Check that the file name has the correct extension for the available types

        int type_num = -1;
        int position = -1;

        for(int i = 0; i < 3; ++i)

        {

          Matcher matcher = Pattern.compile( pattern[i] ).matcher( name );

          while ( matcher.find() )

          { 

            int ival = +matcher.start();

            if (ival >= 0)

            { 

              type_num = i; 
              position = ival; 

            }

         }

       }

       // Show error message if the file does not have a suitable extension fot the type

       if( type_num < 0 || position < 0 )

       {

         utils.showerr( upper, "ERROR:\nImage file type must be \".gif\", \".jpg\", \".png\"" );
         return;

       }

        // Create an image having the correct dimensions

        int w = image_frame.getSize().width;
        int h = image_frame.getSize().height;

        BufferedImage graph_display = new BufferedImage( w, h, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = graph_display.createGraphics();

        // Transfer the diplay contenets to the image and write to the file

        image_frame.paint(g2);
        g2.drawImage( graph_display, 0, 0, w, h, null);
        ImageIO.write( graph_display, type[type_num], file);

      }

      catch (Exception e) { utils.showerr( upper, "ERROR:\nCreating image file" ); }

    }

    else { utils.showerr( upper, "ERROR\nDisplay results not yet created" ); }

  }

  // Read in time / imedance pairs from an ASCII file

  private void read_model_file()

  {

    // Display the file dialog

    JFileChooser chooser = new JFileChooser( new File(pwd) );
    chooser.setDialogTitle("Open model");
    int result = chooser.showOpenDialog(this);

    if (result == JFileChooser.CANCEL_OPTION) return;

    try 

    {

      // Load the file into a buffer

      File file = chooser.getSelectedFile();
      BufferedReader reader = new BufferedReader( new FileReader( file ) );

      // Append each line into a stringbuilder with a newline character

      StringBuilder indata = new StringBuilder();
      String line;

      while( ( line = reader.readLine() ) != null ) { indata.append( line ); indata.append( "\n" ); }

      // close the input file and load the string builder into the text area

      reader.close();

      blk_model.setText( indata.toString() );

    }

    catch (Exception e)

    {

      utils.showerr( upper, "ERROR:\nReading Model File" );

    }

  }

  //  Save the current time / impedance pairs with asscoiated comments

  private void save_model_file()

  {

    // Display the file dialog

    JFileChooser chooser = new JFileChooser( new File(pwd) );
    chooser.setDialogTitle("Save model");

    int result = chooser.showSaveDialog(this);

    if (result == JFileChooser.CANCEL_OPTION) return;

    try

    {

      File file = chooser.getSelectedFile();

      file.createNewFile();

      // create FileWriter and PrintWriter Objects
  
      FileWriter writer = new FileWriter(file); 
      PrintWriter pw    = new PrintWriter(writer);

      // Write the whole textarea containing the model in order to preserve comments

      pw.printf("%s\n",blk_model.getText() ); 

      // Close file

      writer.flush();
      writer.close();

    }

    catch (Exception e) { utils.showerr( upper, "ERROR:\nWriting model file" ); }

  }

  // Save the trace values of the unfiltered and filtered blocks

  private void save_blktrc_file()

  {

    if( blk_filter != null )

    {

      // Display the file dialog

      JFileChooser chooser = new JFileChooser( new File(pwd) );

      chooser.setDialogTitle("Save layer traces");

      int result = chooser.showSaveDialog(this);

      if (result == JFileChooser.CANCEL_OPTION) return;

      try

      {

        File file = chooser.getSelectedFile();

        file.createNewFile();

        // create FileWriter and PrintWriter Objects
  
        FileWriter writer = new FileWriter(file); 
        PrintWriter pw    = new PrintWriter(writer);

       // Create a trace from the input model time / impedance pairs

        double [] times = new double[(intimes.length - 1) * 2 + 4];
        double [] model = new double[(intimes.length - 1) * 2 + 4];

         models2plot ( intimes, impedances, blk_filter.trace_times, times, model, "blk" );

        // Write the traces

        utils.wrt2xgraph ( pw, "Input Block Model",    times, model );
        utils.wrt2xgraph ( pw, "Filtered Block Model", blk_filter.trace_times, blk_filter.filtered_blocks );

        // Close file

        writer.flush();
        writer.close();

      }

      catch (Exception e) { utils.showerr( upper, "WARNING\nUnable to open file" ); }

    }

    else { utils.showerr( upper, "ERROR\nResults not yet created" ); }

  }

  // Save the reflection coefficient values of the unfiltered and filtered blocks

  private void save_rctrc_file()

  {

    if( blk_filter != null )

    {

      // Display the file dialog

      JFileChooser chooser = new JFileChooser( new File(pwd) );
      chooser.setDialogTitle("Save RC traces");

      int result = chooser.showSaveDialog(this);

      if (result == JFileChooser.CANCEL_OPTION) return;

      try

      {

        File file = chooser.getSelectedFile();

        file.createNewFile();

        // create FileWriter and PrintWriter Objects
  
        FileWriter writer = new FileWriter(file); 
        PrintWriter pw    = new PrintWriter(writer);

        // Create a trace of RC derived from the input model time / impedance pairs

        double [] times = new double[intimes.length * 3 + 2];
        double [] model = new double[intimes.length * 3 + 2];

        models2plot ( intimes, blk_filter.rcs, blk_filter.trace_times, times, model, "rc" );

        // Write the traces

        utils.wrt2xgraph ( pw, "RC Model",    times, model );
        utils.wrt2xgraph ( pw, "Filtered RC Model", blk_filter.trace_times,  blk_filter.filtered_rc );

        // Close file

        writer.flush();
        writer.close();

      }

      catch (Exception e) { utils.showerr( upper, "WARNING\nUnable to open file" ); }

    }

    else { utils.showerr( upper, "ERROR\nResults not yet created" ); }

  }

  // Save the analytical and DFT spectra of the filtered blocks

  private void save_blkspec_file()

  {

    if( blk_filter != null )

    {

      // Display the file dialog

      JFileChooser chooser = new JFileChooser( new File(pwd) );
      chooser.setDialogTitle("Save layer spectra");

      int result = chooser.showSaveDialog(this);

      if (result == JFileChooser.CANCEL_OPTION) return;

      try

      {

        File file = chooser.getSelectedFile();

        file.createNewFile();

        // create FileWriter and PrintWriter Objects
  
        FileWriter writer = new FileWriter(file); 
        PrintWriter pw    = new PrintWriter(writer);

        // Save the Filtered block spectra

        utils.wrt2xgraph ( pw, "Blocks Analytical Spectrum",   blk_filter.freqs, blk_filter.spectrum_blocks );
        utils.wrt2xgraph ( pw, "Blocks Windowed DFT Spectrum", blk_filter.freqs, blk_filter.windowed_blk_spectrum );

        // Close file

        writer.flush();
        writer.close();

      }

      catch (Exception e) { utils.showerr( upper, "WARNING\nUnable to open file" ); }

    }

    else { utils.showerr( upper, "ERROR\nResults not yet created" ); }

  }

// Save the analytical and DFT spectra of the reflection coefficients

  private void save_rcspec_file()

  {

    if( blk_filter != null )

    {

      // Display the file dialog

      JFileChooser chooser = new JFileChooser( new File(pwd) );
      chooser.setDialogTitle("Save RC spectra");

      int result = chooser.showSaveDialog(this);

      if (result == JFileChooser.CANCEL_OPTION) return;

      try

      {

        File file = chooser.getSelectedFile();

        file.createNewFile();

        // create FileWriter and PrintWriter Objects
  
        FileWriter writer = new FileWriter(file); 
        PrintWriter pw    = new PrintWriter(writer);

        // Save the filtered RC spectra

        utils.wrt2xgraph ( pw, "RC Analytical Spectrum",   blk_filter.freqs, blk_filter.spectrum_rc );
        utils.wrt2xgraph ( pw, "RC Windowed DFT Spectrum", blk_filter.freqs, blk_filter.windowed_rc_spectrum );

        // Close file

        writer.flush();
        writer.close();

      }

      catch (Exception e) { utils.showerr( upper, "WARNING\nUnable to open file" ); }

    }

    else { utils.showerr( upper, "ERROR\nResults not yet created" ); }

  }

  // Block display object

  class block_plot extends JPanel

  {

    protected void paintComponent(Graphics g)

    { 

      // Initialize graphics

      super.paintComponent(g);  
      Graphics2D g2 = (Graphics2D)g;  
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  
                          RenderingHints.VALUE_ANTIALIAS_ON);  

      // Get the dimensions of the panel

      int w = getWidth();  
      int h = getHeight();   

      if( blk_filter != null )

      { 

        // Create a trace from the input model time / impedance pairs

        double [] times = new double[(intimes.length - 1) * 2 + 4];
        double [] model = new double[(intimes.length - 1) * 2 + 4];

        models2plot ( intimes, impedances, blk_filter.trace_times, times, model, "blk" );

        // Store the current foreground colour

        Color c = this.getForeground();

        // Set up the plot parameters

        double xmin = utils.minval( blk_filter.trace_times);
        double xmax = utils.maxval( blk_filter.trace_times);
        double ymin = utils.minval( blk_filter.filtered_blocks);
        double ymax = utils.maxval( blk_filter.filtered_blocks);

        double yminm = utils.minval( model );
        double ymaxm = utils.maxval( model);

        ymin = ( ymin < yminm )? ymin : yminm;
        ymax = ( ymax > ymaxm )? ymax : ymaxm;

        plot_parameters plot_pars = new plot_parameters(g2, 0.0, 0.0, (double)w, (double)h,
                                                        xmin, xmax, ymin, ymax);

        // Draw the axes

        plot_pars.plot_axes( g2, "Time(ms)", "Layering" );

        // Draw the unfiltered model

        g2.setPaint(Color.red);

        plot_pars.plot_trace ( g2,times, model );

        // Draw the filtered block trace

        g2.setPaint(Color.blue);

        plot_pars.plot_trace ( g2, blk_filter.trace_times, blk_filter.filtered_blocks );
        plot_pars.plot_points( g2, blk_filter.trace_times, blk_filter.filtered_blocks, 4 );

        // Draw the DFT window

        g2.setPaint(Color.green);

        show_spec_window ( g2, plot_pars, c );

        // Rest the foreground colour

        g2.setPaint(c);

      }

    }

  }

  // RC display object

  class rc_plot extends JPanel

  {

    protected void paintComponent(Graphics g)

    { 

      // Initialize graphics

      super.paintComponent(g);  
      Graphics2D g2 = (Graphics2D)g;  
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  
                          RenderingHints.VALUE_ANTIALIAS_ON);  

      // Get the dimensions of the panel

      int w = getWidth();  
      int h = getHeight();   

      if( blk_filter != null )

      { 

        // Store the current foreground colour

        Color c = this.getForeground();

        // Create a trace from the RC derived from the input model time / impedance pairs

        double [] times = new double[intimes.length * 3 + 2];
        double [] model = new double[intimes.length * 3 + 2];

        models2plot ( intimes, blk_filter.rcs, blk_filter.trace_times, times, model, "rc" );

        // Set up the plot parameters

        double xmin = utils.minval( blk_filter.trace_times);
        double xmax = utils.maxval( blk_filter.trace_times);
        double ymin = utils.minval( blk_filter.filtered_rc);
        double ymax = utils.maxval( blk_filter.filtered_rc);

        double yminm = utils.minval( model );
        double ymaxm = utils.maxval( model);

        ymin = ( ymin < yminm )? ymin : yminm;
        ymax = ( ymax > ymaxm )? ymax : ymaxm;

        plot_parameters plot_pars = new plot_parameters(g2, 0.0, 0.0, (double)w, (double)h,
                                                        xmin, xmax, ymin, ymax);

        // Draw the axes

        plot_pars.plot_axes( g2, "Time(ms)", "Reflectivity" );

        // Draw the unfiltered model RCs

        g2.setPaint(Color.red);

        plot_pars.plot_trace ( g2,times, model );

        // Draw the filtered RC trace

        g2.setPaint(Color.blue);

        plot_pars.plot_trace ( g2, blk_filter.trace_times, blk_filter.filtered_rc );
        plot_pars.plot_points( g2, blk_filter.trace_times, blk_filter.filtered_rc, 4 );

        // Draw the DFT window

        g2.setPaint(Color.green);

        show_spec_window ( g2, plot_pars, c );

        // Rest the foreground colour

        g2.setPaint(c);

      }

    }

  }

  // Block spectra display object

  class blk_spectra_plot extends JPanel

  {

    protected void paintComponent(Graphics g)

    { 

      // Initialize graphics

      super.paintComponent(g);  
      Graphics2D g2 = (Graphics2D)g;  
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  
                          RenderingHints.VALUE_ANTIALIAS_ON);  

      // Get the dimensions of the panel

      int w = getWidth();  
      int h = getHeight();   

      if( blk_filter != null )

      { 

        // Store the current foreground colour

        Color c = this.getForeground();

        // Set up the plot parameters

        double xmin  = utils.minval( blk_filter.freqs);
        double xmax  = utils.maxval( blk_filter.freqs);
        double ymin  = utils.minval( blk_filter.spectrum_blocks);
        double ymax  = utils.maxval( blk_filter.spectrum_blocks);
        double yminw = utils.minval( blk_filter.windowed_blk_spectrum);
        double ymaxw = utils.maxval( blk_filter.windowed_blk_spectrum);

        ymin = ( ymin < yminw )? ymin : yminw;
        ymax = ( ymax < ymaxw )? ymax : ymaxw;

        plot_parameters plot_pars = new plot_parameters(g2, 0.0, 0.0, (double)w, (double)h,
                                                        xmin, xmax, ymin, ymax);

        // Draw the axes

        plot_pars.plot_axes( g2, "Frequency (hz)", "Amplitude Spectra of Filtered Layers" );

        // Draw the Analytical spectrum of the filtered blocks

        g2.setPaint(Color.blue);

        plot_pars.plot_trace ( g2, blk_filter.freqs, blk_filter.spectrum_blocks );

        // Draw the windowed spectrum of the filtered blocks

        g2.setPaint(Color.green);

        plot_pars.plot_trace ( g2, blk_filter.freqs, blk_filter.windowed_blk_spectrum );

        // Rest the foreground colour
 
        g2.setPaint(c);

        // Draw the legend for the windowed spectrum

        String label = "Sampled Window DFT Spectrum";

        // Get the font details and determine the dimensions of the label

        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();

        double str_width  = font.getStringBounds( label, frc ).getWidth();
        double str_height = font.getStringBounds( label, frc ).getHeight();

        // Draw the label

        g2.drawString( label, (int)(plot_pars.Minx), (int)( plot_pars.Maxy + str_height * 4.0 ));

        // Draw a line of the appropriate colour foe the label parameters

        g2.setPaint(Color.green);

        int lstartx = (int)(plot_pars.Minx + str_width + 10);
        int lendx = lstartx + 50;

        int lstarty = (int)( plot_pars.Maxy + str_height * 3.5 );

        g2.drawLine( lstartx, lstarty, lendx , lstarty); 

        g2.setPaint(c);

        // Draw the legend for the analytical spectrum

        label = "Analytical Spectrum";

        // Get the font details and determine the dimensions of the label

        str_width  = font.getStringBounds( label, frc ).getWidth();
        str_height = font.getStringBounds( label, frc ).getHeight();

        // Draw the label

        g2.drawString( label, (int)plot_pars.Minx, (int)( plot_pars.Maxy + str_height * 3.0 ));

        // Draw a line of the appropriate colour foe the label parameters

        g2.setPaint(Color.blue);

        lstarty -= (int)str_height;

        g2.drawLine( lstartx, lstarty, lendx , lstarty); 

        // Rest the foreground colour

        g2.setPaint(c);

      }

    }

  }

  // RC spectra display object

  class rc_spectra_plot extends JPanel

  {

    protected void paintComponent(Graphics g)

    { 

      // Initialize graphics

      super.paintComponent(g);  
      Graphics2D g2 = (Graphics2D)g;  
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  
                          RenderingHints.VALUE_ANTIALIAS_ON);  

      // Get the dimensions of the panel

      int w = getWidth();  
      int h = getHeight();   

      if( blk_filter != null )

      { 

        // Store the current foreground colour

        Color c = this.getForeground();

        // Set up the plot parameters

        double xmin  = utils.minval( blk_filter.freqs);
        double xmax  = utils.maxval( blk_filter.freqs);
        double ymin  = utils.minval( blk_filter.spectrum_rc);
        double ymax  = utils.maxval( blk_filter.spectrum_rc);
        double yminw = utils.minval( blk_filter.windowed_rc_spectrum);
        double ymaxw = utils.maxval( blk_filter.windowed_rc_spectrum);

        ymin = ( ymin < yminw )? ymin : yminw;
        ymax = ( ymax < ymaxw )? ymax : ymaxw;

        plot_parameters plot_pars = new plot_parameters(g2, 0.0, 0.0, (double)w, (double)h,
                                                        xmin, xmax, ymin, ymax);

        // Draw the axes

        plot_pars.plot_axes( g2, "Frequency (hz)", "Amplitude Spectra of Reflectivity" );

        // Draw the Analytical spectrum of the filtered RCs

        g2.setPaint(Color.blue);

        plot_pars.plot_trace ( g2, blk_filter.freqs, blk_filter.spectrum_rc );

        // Draw the windowed DFT spectrum of the filtered RCs

        g2.setPaint(Color.green);

        plot_pars.plot_trace ( g2, blk_filter.freqs, blk_filter.windowed_rc_spectrum );

        // Rest the foreground colour
 
        g2.setPaint(c);

        // Draw the legend for the windowed spectrum

        String label = "Sampled Window DFT Spectrum";

        // Get the font details and determine the dimensions of the label

        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();

        double str_width  = font.getStringBounds( label, frc ).getWidth();
        double str_height = font.getStringBounds( label, frc ).getHeight();

        // Draw the label

        g2.drawString( label, (int)(plot_pars.Minx), (int)( plot_pars.Maxy + str_height * 4.0 ));

        // Draw a line of the appropriate colour foe the label parameters

        g2.setPaint(Color.green);

        int lstartx = (int)(plot_pars.Minx + str_width + 10);
        int lendx = lstartx + 50;

        int lstarty = (int)( plot_pars.Maxy + str_height * 3.5 );

        g2.drawLine( lstartx, lstarty, lendx , lstarty); 

        // Rest the foreground colour

        g2.setPaint(c);

        label = "Analytical Spectrum";

        // Get the font details and determine the dimensions of the label

        str_width  = font.getStringBounds( label, frc ).getWidth();
        str_height = font.getStringBounds( label, frc ).getHeight();

        // Draw the label

        g2.drawString( label, (int)plot_pars.Minx, (int)( plot_pars.Maxy + str_height * 3.0 ));

        // Draw a line of the appropriate colour foe the label parameters

        g2.setPaint(Color.blue);

        lstarty -= (int)str_height;

        g2.drawLine( lstartx, lstarty, lendx , lstarty); 

        // Rest the foreground colour

        g2.setPaint(c);

        // Display a time stamp

        DateFormat simple = DateFormat.getInstance();
        String now = simple.format( new Date() );

        String copy = String.format("%s: %s","OpenGeosolutions",now);

        // Get the font details and determine the dimensions of the label

        str_width  = font.getStringBounds( copy, frc ).getWidth();
        str_height = font.getStringBounds( copy, frc ).getHeight();

        // Drav the time stamp

        g2.drawString( copy, w - (int)str_width, h );

      }

    }

  }

  // Show the spec window as a bar below the time axis

  void show_spec_window ( Graphics2D g2, plot_parameters plot_pars, Color c )

  {

    // Calculate the location of the displayed window from the plot parameters

    int topx = (int)(plot_pars.Minx + (dft_window_top - plot_pars.Mintickx) * plot_pars.Scalex);
    int botx = (int)(plot_pars.Minx + (dft_window_bot - plot_pars.Mintickx) * plot_pars.Scalex);

   // Add a label

    String tval = String.format("DFT WINDOW");

    // Get the font details and determine the dimensions of the label

    FontRenderContext frc = g2.getFontRenderContext();
    Font font = g2.getFont();
    double tval_width  = font.getStringBounds( tval, frc ).getWidth();
    double tval_height = font.getStringBounds( tval, frc ).getHeight();

    int offsety = ( plot_pars.Minticky > 0.0 )? (int)(tval_height * 1.5) : 0;  // value of y where y crosses x axis

    int topy = (int)plot_pars.Maxy + 10 + offsety;

    int width  = botx - topx;
    int height = 5;

    // Draw a filled rectangle of the current foreground colour

    g2.fillRect( topx, topy, width, 5 );

    // Reset the foreground colour

    g2.setPaint(c);

    // draw a bounding rectangle

    g2.drawRect( topx, topy, width, 5 );

    g2.drawLine( topx, topy - height , topx, topy + 2 * height );
    g2.drawLine( botx, topy - height , botx, topy + 2 * height );

    // Draw the label

    g2.drawString(tval, topx + (width - (int)tval_width) / 2, topy + height + (int)tval_height);

  }

  // Create plootable traces from time / value pairs at block boundaries

  void models2plot ( double [] intimes, double [] impedances, double [] outtimes, 
                     double [] times, double [] model, String type )

  {

    if( type.equalsIgnoreCase("rc") )  // Create a display trace from the RC values

    {

      times[0] = outtimes[0];
      model[0] = 0.0;

      int icount = 1;

      for( int i = 0; i < intimes.length; ++i)

      {

        times[icount] = intimes[i] / UnitSc;
        model[icount] = 0.0;
        ++icount;

        times[icount] = intimes[i] / UnitSc;
        model[icount] = impedances[i];
        ++icount;

        times[icount] = intimes[i] / UnitSc;
        model[icount] = 0.0;
        ++icount;

      }

      times[icount] = outtimes[outtimes.length - 1];
      model[icount] = 0.0;

    }

    else                               // Create a display trace from the block model

    {

      times[0] = outtimes[0];
      model[0] = impedances[0];

      int icount = 1;

      for( int i = 0; i < intimes.length - 1; ++i)

      {

        times[icount] = intimes[i] / UnitSc;
        model[icount] = impedances[i];
        ++icount;

        times[icount] = intimes[i] / UnitSc;
        model[icount] = impedances[i + 1];
        ++icount;

      }

       times[icount] = intimes[intimes.length - 1] / UnitSc;
       model[icount] = impedances[intimes.length - 1];
       ++icount;

       times[icount] = intimes[intimes.length - 1] / UnitSc;
       model[icount] = impedances[0];
       ++icount;

       times[icount] = outtimes[outtimes.length - 1];
       model[icount] = impedances[0];

    }

  }

  // Panel for displaying runtim parameters

  class Gridbagin extends JPanel implements ActionListener

  {

    GridBagConstraints constraints = new GridBagConstraints();
    JButton ok;
    JButton quit;

    final JTextField f1;
    final JTextField f2;
    final JTextField f3;
    final JTextField f4;
    final JTextField dt;
    final JTextField win_t;
    final JTextField win_b;
    final JTextField taper;

    public Gridbagin ()

    {

      // Set default value of the calulate flag to false

      calculate = false;

      // Change the background colour of the panel

      setBackground(Color.cyan);

      // Initialize the gridbag layout parameters

      setLayout( new GridBagLayout() );

      constraints.weightx = 0.0;
      constraints.weighty = 0.0;
      constraints.fill = GridBagConstraints.BOTH;

    // Initialise coordinates for adding to the frame gridbag layout

      int x = 0;
      int y = 0;

      // Create first corner frequency label and text box, then add to gridbag

      constraints.gridwidth = 2;
      f1 = new JTextField();
      f1.setFont( new Font("Serif", Font.BOLD, 12));
      f1.setText("");
      addGB( f1, x = 2, y = 0);

      final JLabel label_f1 = new JLabel();
      label_f1.setFont( new Font("Serif", Font.BOLD, 12));
      label_f1.setText("FILTER CORNER 1 (Hz)");
      addGB( label_f1, x = 0, y);

      // Create second corner frequency label and text box, then add to gridbag

      f2 = new JTextField();
      f2.setFont( new Font("Serif", Font.BOLD, 12));
      f2.setText("");
      addGB( f2, x = 2, y += 2);

      final JLabel label_f2 = new JLabel();
      label_f2.setFont( new Font("Serif", Font.BOLD, 12));
      label_f2.setText("FILTER CORNER 2 (Hz)");
      addGB( label_f2, x = 0, y);

      // Create third corner frequency label and text box, then add to gridbag

      f3 = new JTextField();
      f3.setFont( new Font("Serif", Font.BOLD, 12));
      f3.setText("");
      addGB( f3, x = 2, y += 2);

      final JLabel label_f3 = new JLabel();
      label_f3.setFont( new Font("Serif", Font.BOLD, 12));
      label_f3.setText("FILTER CORNER 3 (Hz)");
      addGB( label_f3, x = 0, y);

      // Create fourth corner frequency label and text box, then add to gridbag

      f4 = new JTextField();
      f4.setFont( new Font("Serif", Font.BOLD, 12));
      f4.setText("");
      addGB( f4, x = 2, y += 2);

      final JLabel label_f4 = new JLabel();
      label_f4.setFont( new Font("Serif", Font.BOLD, 12));
      label_f4.setText("FILTER CORNER 4 (Hz)");
      addGB( label_f4, x = 0, y);

      // Create sample interval label and text box, then add to gridbag

      dt = new JTextField();
      dt.setFont( new Font("Serif", Font.BOLD, 12));
      dt.setText("");
      addGB( dt, x = 2, y += 2);
      constraints.gridwidth = 1;

      final JLabel label_dt = new JLabel();
      label_dt.setFont( new Font("Serif", Font.BOLD, 12));
      label_dt.setText("OUTPUT SAMPLE INTERVAL (ms)");
      addGB( label_dt, x = 0, y);

      // Create DFT window labels and text boxs, then add to gridbag

      win_t = new JTextField();
      win_t.setFont( new Font("Serif", Font.BOLD, 12));
      win_t.setText("");
      addGB( win_t, x = 2, y += 2);

      final JLabel label_win_t = new JLabel();
      label_win_t.setFont( new Font("Serif", Font.BOLD, 12));
      label_win_t.setText("DFT WINDOW START (ms)");
      addGB( label_win_t, x = 0, y);

      win_b = new JTextField();
      win_b.setFont( new Font("Serif", Font.BOLD, 12));
      win_b.setText("");
      addGB( win_b, x = 2, y += 2);

      final JLabel label_win_b = new JLabel();
      label_win_b.setFont( new Font("Serif", Font.BOLD, 12));
      label_win_b.setText("DFT WINDOW END    (ms)");
      addGB( label_win_b, x = 0, y);

      taper = new JTextField();
      taper.setFont( new Font("Serif", Font.BOLD, 12));
      taper.setText("10");
      addGB( taper, x = 2, y += 2);

      final JLabel label_taper = new JLabel();
      label_taper.setFont( new Font("Serif", Font.BOLD, 12));
      label_taper.setText("DFT COSINE TAPER %");
      addGB( label_taper, x = 0, y);

      // Create control buttons and add to the gridbag

      constraints.gridheight = 2;
      constraints.fill = GridBagConstraints.NONE;

      ok = new JButton("OK");
      ok.setBackground(Color.green);
      ok.addActionListener( this );
      addGB( ok, x = 0, y += 2);
   
      quit = new JButton("QUIT");
      quit.setBackground(Color.red);
      quit.addActionListener( this );
      addGB( quit, x = 2, y);

    }

  // Method to add a component to a gridbag at a specified x,y

    void addGB( Component component, int x, int y)

    {

      constraints.gridx = x;
      constraints.gridy = y;
      add( component, constraints);

    }

  // Call back for mouse action in a button

    public void actionPerformed( ActionEvent e )

    {

      if ( e.getSource() == quit) { System.exit(0); } // Quit Button pushed

      if ( e.getSource() == ok)

      {

        // If inoput parameter values are numeric then assign them to their respective variables

        calculate = true;
        filter  = new double[4];

        if( utils.isNumeric( f1.getText() ) )    { filter[0] = Double.parseDouble(f1.getText()); }
        else                                     { calculate = false;                            }

        if( utils.isNumeric( f2.getText() ) )    { filter[1] = Double.parseDouble(f2.getText()); }
        else                                     { calculate = false;                            }

        if( utils.isNumeric( f3.getText() ) )    { filter[2] = Double.parseDouble(f3.getText()); }
        else                                     { calculate = false;                            }

        if( utils.isNumeric( f4.getText() ) )    { filter[3] = Double.parseDouble(f4.getText()); }
        else                                     { calculate = false;                            }

        if( utils.isNumeric( dt.getText() ) )    { dsamp = Double.parseDouble(dt.getText()); }
        else                                     { calculate = false;                            }

        if( utils.isNumeric( taper.getText() ) ) { taper_percent = Double.parseDouble(taper.getText()); }
        else                                     { calculate = false;                                   }

        if( utils.isNumeric( win_t.getText() ) ) { dft_window_top = Double.parseDouble(win_t.getText()); }
        else                                     { calculate = false;                                    }

        if( utils.isNumeric( win_b.getText() ) ) { dft_window_bot = Double.parseDouble(win_b.getText()); }
        else                                     { calculate = false;                                    }

        // If parameters values  are valid numbers check that they are appropriate 

        if ( calculate )

        {

          // Check that the filter corners are > 0.0 and in increasing order

          if ( filter[0] < 0.0 ) { calculate = false; }
          else { for( int i = 1; i < filter.length; ++i ) { if (filter[i] < filter[ i - 1] ) { calculate = false; } } }

          calculate = ( dsamp < 0.0 )?                     false : calculate;
          calculate = ( taper_percent < 0.0 )?             false : calculate;
          calculate = ( dft_window_bot < dft_window_top )? false : calculate;

        }

        // Scaler from input milliseconds to seconds

        UnitSc = 1.0e-3;  

        // If input variables are suitable read the block model

        if( calculate )

        {

          // Determine the number of time / impedance pairs.

          int nvals = validate_model (blk_model.getText());

          if( ( nvals > 0 ) && ( nvals % 2 == 0 ) )

          {

            // Allocate space for the input time / impedance vectors and initialise them

            int nblocks = nvals / 2;
            impedances  = new double[nblocks];
            intimes     = new double[nblocks];

            for(int i = 0; i < nblocks; ++i ) { intimes[i] = 0.0 ;impedances[i] = 0.0;}

            // Load the time /impedance vectors

            load_model (blk_model.getText());

            // Check that the model times are in increasin order

            for(int i = 1; i < nblocks; ++i ) { calculate = (intimes[i] > intimes[i - 1] )? calculate : false;  }

            if( calculate )

            {

              // Create a Blockfilter object and calulate the associated spectra and filtered traces

              blk_filter = new Blockfilter(filter, impedances, intimes, UnitSc,
                                           dsamp, taper_percent, dft_window_top, dft_window_bot);

              // If sample interval causes aliassing ahow a warning

              if(blk_filter.aliasflag) 

              {

                utils.showerr( upper, "WARNING\nAliasing with this sample interval / filter combination" );

              }

              // Redraw the displays

              blk_trace.repaint();
              rc_trace.repaint();
              blk_spectra.repaint();
              rc_spectra.repaint();

            }

            else  // Show warning for inconsistent lengths for the time / impedance vectors

            {

              utils.showerr(lower,"ERROR:\nModel times are not in increasing order");

            }
 
          }

          else  // Show warning for inconsistent lengths for the time / impedance vectors

          {

            utils.showerr(lower,"ERROR:\nEqual numbers of numerical Time/Impedance values required");

          }

        }

        else  // Show warning for error in parameters.

        {

          utils.showerr(upper,"ERROR:\nAn input parameter has errors or is not defined as a numeric value");

        }

      }

    }

   // Read and parse model data from a file containing commnets

    public void load_model (String str)

    {

      int nvals = 0;
      int count = 0;

      // Split into a series of lines
      
      String [] textlines = str.split("\n");

      // Strip out comments from each line in turn

      for( int i = 0; i < textlines.length; ++i )

      {

        // Remove leading and training white space

        String line = textlines[i].trim();

        // Split line at the comment

        String [] no_comments = line.split("[#/]+");

        // Analyse the line prior to the comments

        if( no_comments.length > 0) 

        { 

          // Split up the current line into a series of delimiter separated numbers

          String [] numerics = no_comments[0].split("[ ,\t]+");

          // Test that that the first character is not null (blank line)

          StringReader sr = new StringReader ( numerics[0] );

          char test = '\0';

          try                   { test = (char)sr.read(); }
          catch (IOException e) { utils.showerr(lower,"ERROR:\nParsing error on while loading model"); }

          if ( (byte)test > 0)

          {

            // Accumulate the total number of time / imedance pairs found in the input pane

            for(int k = 0; k < numerics.length; ++k)

            { 

              if(nvals % 2 > 0) { impedances[count] = Double.parseDouble(numerics[k]); }
              else              { intimes[count]    = Double.parseDouble(numerics[k]); }

              ++nvals;
              count = nvals / 2;

            }

          }

        }

      }

      return;

    }

    // Determine how many number fields are in the input pane

    public int validate_model (String str)

    {

      boolean valid_data = true;
      int nvals = 0;

      // Split into a series of lines
      
      String [] textlines = str.split("\n");

      // Strip out comments from each line in turn

      for( int i = 0; i < textlines.length; ++i )

      {

        // Remove leading and training white space

        String line = textlines[i].trim();

        // Split line at the comment

        String [] no_comments = line.split("[#/]+");

        // Analyse the line prior to the comments

        if(no_comments.length > 0) 

        { 

          // Split up the current line into a series of delimiter separated numbers

          String [] numerics = no_comments[0].split("[ ,\t]+");

          // Test that that the first character is not null

          StringReader sr = new StringReader ( numerics[0] );

          char test = '\0';

          try                   { test = (char)sr.read(); }
          catch (IOException e) { utils.showerr(lower,"ERROR:\nParsing error on while loading model"); }

          if ( (byte)test > 0)

          {

            // Accumulate the number of valid numerical values in the pane

            for(int k = 0; k < numerics.length; ++k)

            { 

              ++nvals;
             valid_data = ( utils.isNumeric( numerics[k]) )? valid_data : false;

            }

          }

        }

      }

      // Check that there is an even number of values since we want time / impedance pairs

      valid_data = ( nvals % 2 > 0)? false : valid_data;

      // Return the number of valid number fields in the pane.

      return (valid_data)? nvals : 0;

    }

  }

}

