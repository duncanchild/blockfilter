package org.ogs.bf;
/*

                                 Help

Displays 'filter_layers' menu item 'help' components in html format.

Resource files used:

  help_filter_layers.html
  about_filter_layers.html

File Contains:

  public class Help extends JFrame implements ActionListener

  Contains:

    public Help( int help_choice )  Class constructor for Help
                                    help choice = 0 loads "help_filter_layers.html"
                                    help choice = 1 loads "about_filter_layers.html"

    void addGB( Component component, int x, int y) Adds a component to a gridbag layout

    public void actionPerformed( ActionEvent e ) Mouse click detector

    protected void openURL ( String str ) Opens a url into the frame

  Requires:

    utilities

  class LinkActivator implements HyperlinkListener

  Contains:

    public void hyperlinkUpdate( HyperlinkEvent he ) Action on hyperlink detection

  ########################## COPYRIGHT NOTICE: ###################################

  Copyright 2013, OpenGeoSolutions, All rights reserved
  Any comments/suggestions for improvement should be made to mdbush@opengeosolutions.com

  Author: M.D. Bush

*/

import java.io.*;
import java.net.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

import javax.swing.text.html.*;
import javax.swing.event.*;

@SuppressWarnings("serial")

public class Help extends JFrame implements ActionListener

{

  JButton dismiss;
  JButton prev;
  JButton next;
  JPanel upper;
  protected JEditorPane version;
  LinkedList<String> history;

  int item;
 
  GridBagConstraints constraints = new GridBagConstraints();

  // Class constructor
  
  public Help( int help_choice )

  {

    // Initialise history indicators

    history = new LinkedList<String>();
    item = 0;

    // Ensure a valid choice to select resource files

    help_choice =  (int)Math.abs( (double)help_choice );
    help_choice = ( help_choice > 1 )? 1 : help_choice;

    // Define resource files

    String [] textfiles = { "Docs/help_filter_layers.html", "Docs/about_filter_layers.html" };

    // Set up the view frame according to selected html file

    int [] frame_width  = { 400, 400 };
    int [] frame_height = { 600, 500 };

    this.setSize( frame_width[help_choice], frame_height[help_choice]);
    this.setLocation(100,100);

    // Set up the base url with the appropriate filename

    StringBuilder urlbuilder = new StringBuilder();
    urlbuilder.append( "file:" );
    urlbuilder.append( textfiles[help_choice] );

    // Set up a gridbag layout for the frane

    this.setLayout( new GridBagLayout() );

    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridwidth = 1;
    constraints.gridheight = 1;

    // Create the objects that will go into the frame layout

    JToolBar toolbar = new JToolBar();
    upper            = new JPanel();
    JPanel lower     = new JPanel();

    // Initialise coordinates for adding to the frame gridbag layout

    int x = 0;
    int y = 0;

    // Create "back" and "forward buttons and add them to the toolbar

    prev = new JButton("back");
    next = new JButton("foward");
    toolbar.add(prev);
    toolbar.add(next);
    prev.addActionListener( this );
    next.addActionListener( this );

    // Only add the toolbar when "help_filter_layers.html" is loaded

    if( help_choice == 0 ) { addGB( toolbar, x, y); }

    // Set the main pane layout to be a single grid

    upper.setLayout(new GridLayout(1,1));

    // Create JEditorPane ( allows html to be loaded ) with a scrollbar

    version = new JEditorPane();
    version.setEditable(false);
    upper.add(new JScrollPane(version), BorderLayout.CENTER);
    version.addHyperlinkListener ( new LinkActivator() );

    // Add the select html file to the display list and display it.

    String urlname = urlbuilder.toString();
    history.add( urlname );
    openURL( urlname );

    // Add the html display to the frame gridbag layout

    constraints.weighty = 40.0;
    ++y;
    addGB( upper, x, y);

    // Create a dismiss button for the bottom component of the frame gridbag
 
    dismiss = new JButton("Close");

    lower.add( dismiss );
    dismiss.addActionListener( this );

    // Add the the bottom component to the frame gridbag

    constraints.weighty = 1.0;
    ++y;
    addGB( lower, x, y );

    this.setVisible(true);

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

    if ( e.getSource() == dismiss ) { setVisible(false); }

    if ( e.getSource() == prev )  // Load the previous item in the linked list

    { 

      item = ( item > 0 )? item - 1 : item;
      openURL( history.get(item) );

    }

    if ( e.getSource() == next ) // Load the next item in the linked list

    { 

      item = ( item == (history.size() - 1) )? item: item + 1;
      openURL( history.get(item) );

    }

  }

  // Opens a URL in the JEditorPane

  protected void openURL ( String str )

  {

    try

    {

      URL url = new URL( str );
      version.setPage( url );

    }

    catch (Exception e)

    {
 
      utilities utils = new utilities();
      utils.showerr( upper, "ERROR:\nhtml error" );

    }

  }

  // Class to detect whether a URL link was selected.

  class LinkActivator implements HyperlinkListener

  {

    public void hyperlinkUpdate( HyperlinkEvent he )

    {

      HyperlinkEvent.EventType type = he.getEventType();

      if (type == HyperlinkEvent.EventType.ACTIVATED )

      { 

        // Load the URL into the JEditorPane

        openURL( he.getURL().toExternalForm() ); 

        // Update the history linked list and current item in it

        history.add( he.getURL().toExternalForm() );
        item = history.size() - 1;

      }

    }

  }

}
