package org.ogs.bf;
/*

                        Blockfilter

  Java class for calculating the analytical and windowed DFT spectra 
  after application of an Ormsby filterof on a series of blocks having
  different acoustic impedances. The filtered block trace is also
  returned along with the reflection coefficients at each interface
  together with with the filtered reflection coeffient trace.
  The analytical spectrum of the reflection coefficient series
  is also returned along with the associated windowed DFT spectrum.

  If spikes (zero length blocks) are detected the reflection coefficients and
  their associated spectra and traces are set to zero.

  Class Members:

    int nsamp;                        No. of samples in the output filtered traces
    int nfreq;                        No. of frequencies in the spectra
    int nspikes;                      No. of reflection Coefficients
  
    boolean aliasflag;                Set to true if selected sample interval is aliased over the frequency range

    double [] trace_times;            Vector of trace sample times
    double [] freqs;                  Vector of frequencies
    double [] spectrum_blocks;        Analytical amplitude spectrum of the filtered blocks
    double [] filtered_blocks;        Filtered trace corresponding to the blocks
    double [] windowed_blk_spectrum;  Windowed DFT spectrum of the filtered blocks
    double [] spectrum_rc;            Analytical amplitude spectrum of the filtered RCs
    double [] filtered_rc;            Filtered reflection coefficient trace
    double [] windowed_rc_spectrum;   Windowed DFT spectrum of the filtered RCs
    double [] rcs;                    Vector of reflection coefficients

  Usage:

  Blockfilter blk_filter = new Blockfilter(filter, impedances, intimes, UnitSc,
                                           dsamp, taper_percent, dft_window_top, dft_window_bot);

  Parameters for class creation:

    double [] filter;                Vector of Ormsby corner frequencies (Hz) in increasing size ( length = 4)
    double [] impedances;            Vector of acoustic impedances. (the first element is taken as the bacground value)
    double [] intimes;               Vector of block end times (usually millseconds)
    double dsamp;                    Trace sample interval (ms).  If <= 0, a suitable sample interval is automatically calulated        
    double UnitSc;                   Scaler to convert the input times to seconds, usually 1.0e-3 (i.e. millseconds to seconds)
    double taper_percent;            Percentage of cosine taper applied to DFT window.
    double dft_window_top;           Start of the DFT window (ms)
    double dft_window_bot;           End of the DFT window (ms).

  Author: M.D. Bush

  Original Aug. 2013 based on Perl Module 'BlockFilter.pm' by M.D. Bush, 2003

  ########################## COPYRIGHT NOTICE: ###################################

  Copyright 2013, OpenGeoSolutions, All rights reserved
  Any comments/suggestions for improvement should be made to mdbush@opengeosolutions.com

*/

import java.lang.*;

public class Blockfilter 

{

  public int nsamp,nfreq,nspikes;
  
  public boolean aliasflag;

  public double [] trace_times;
  public double [] freqs;
  public double [] spectrum_blocks;
  public double [] filtered_blocks;
  public double [] windowed_blk_spectrum;
  public double [] spectrum_rc;
  public double [] filtered_rc;
  public double [] windowed_rc_spectrum;
  public double [] rcs;
  public double dft_window_top;
  public double dft_window_bot;
  public double[] impedances;
  public double[] intimes;

  public Blockfilter ( double [] filter, double [] impedances, double [] intimes, double UnitSc,
                       double dsamp, double taper_percent, double dft_window_top, double dft_window_bot)

  {

	this.impedances = impedances;
	this.intimes = intimes.clone();
	this.dft_window_bot = dft_window_bot;
	this.dft_window_top = dft_window_top;
    nspikes = impedances.length;
    int nblocks =  nspikes - 1;

    double [] widths  = new double[nblocks];
    double [] centres = new double[nblocks];

    rcs               = new double[nspikes];

    // Generate block widths, centres and reflection coefficients 

    boolean rctrue = load_vectors (impedances, intimes, widths, centres, rcs );

    // Find the minimum block thickness

    double min_thick = 1e300;

    for (int i = 0; i < widths.length; ++i) { min_thick =  (min_thick < widths[i])? min_thick : widths[i]; }

    for (int i = 0; i < widths.length; ++i) { widths[i] *= UnitSc; centres[i] *= UnitSc; }

    // Find default sample interval if not defined

    double dsamp_max = 1.0 / (UnitSc * filter[3] * 2.0) ;

    if (dsamp < 1.0e-15)

    {

      dsamp = (min_thick > 1.0)? 1 : (min_thick > 0.0)? min_thick / 2.0 : 0.1;
      dsamp = ( dsamp > dsamp_max )?  dsamp_max : dsamp;                       // prevent aliasing

    }

    for (int i = 0; i < nspikes; ++i) { intimes[i] *= UnitSc;}

    // Set flag if aliasing in the time domain at the selected sample interval

    aliasflag = (dsamp > dsamp_max)? true : false;

    dsamp *= UnitSc;
    double Nyquist = 1.0 / (2.0 * dsamp);

    // Determine an appropriate frequency sampling interval to avoid aliasing

    double interval = (intimes[nblocks] - intimes[0]);
    double dfreq = (interval > 0.0)? Math.abs( 1.0 / ( 8.0 * interval ) ) : 1.0;
    dfreq = (dfreq > 1.0)? 1.0 : dfreq;

   // Determine the trace length and sample interval

    double trace_length = 9.0 / filter[3] + interval;  // 1/ f4 = approx. width of impulse zero crossing
    double centre_time = (intimes[nblocks] + intimes[0]) / 2.0;

    double time_start = centre_time - trace_length;
    double time_end   = centre_time + trace_length;

    dft_window_top *= UnitSc;
    dft_window_bot *= UnitSc;

    time_start = (time_start < dft_window_top)? time_start : dft_window_top;
    time_end   = (time_end   > dft_window_bot)? time_end : dft_window_bot;

    double dft_window = (dft_window_bot - dft_window_top);

    trace_length = time_end - time_start;

    nsamp = (int)(trace_length / dsamp) + 1;
    nsamp = ((nsamp % 2) > 0)? nsamp: nsamp + 1;

   // Create the vector of trace times

    trace_times = new double[nsamp];

    load_trace_times(nsamp, trace_times, dsamp, centre_time);

    // Create and initilise the filter and frequency vectors

    nfreq = (int)(filter[3] / dfreq) + 2;
    freqs = new double[nfreq];

    double [] filtercoefs = new double[nfreq];

    for( int i = 0; i < nfreq; ++i) { freqs[i] = (float)i * dfreq; }

    // Generate smoothed coefficients of an Ormsby filter 

    int smoothtype = 1;  // smoothType = 1 Hanning smoothing otherwise Hamming - Tukey smoothing

    ormsby_coefs( freqs, filter, filtercoefs, nfreq, smoothtype);

    // Create vectors for the block spectrum and the filtered blocks and the DFT spectrum of filtered blocks

    spectrum_blocks       = new double[nfreq];
    filtered_blocks       = new double[nsamp];
    windowed_blk_spectrum = new double[nfreq];

    // Calculate the analytical spectrum and filter the blocks 

    filter_blocks( centres, widths, impedances, freqs, filtercoefs, trace_times, 
                   spectrum_blocks, filtered_blocks, nsamp, nfreq, nblocks);

   // Find the indices of the time trace vector that correspond to the dft window

    int start_index = 0;
    int end_index   = nsamp - 1;

    for( int i = 0; i < nsamp; ++i )

    {

      start_index = (trace_times[i] > dft_window_top)? start_index : i;
      end_index   = (trace_times[i] > dft_window_bot)? end_index : i;

    }

    int nsamp_window = end_index - start_index + 1;

    // Determine the cosine taper weights for the windowed spectra

    double [] taper_weights = new double[nsamp_window];

    for(int i = 0; i < nsamp_window; ++i) { taper_weights[i] = 1.0; }

    cosine_taper ( nsamp_window, taper_percent, taper_weights );

    // Calculate the dft of the windowed block trace

    double freq_scaler = dfreq / (2.0 * Nyquist); // scaler to compare DFT with analytical block spectrum

    windowed_dft (trace_times, freqs, filtered_blocks, taper_weights, nsamp_window, nfreq, 
                  start_index, freq_scaler, windowed_blk_spectrum );

    // replace background if low no filtering at DC 

    if( filter[1] < 1.0e-16) { for( int i = 0; i < nsamp; ++i ) { filtered_blocks[i] += impedances[0]; } }

  // Calculate the filtered Fourier spectrum of the reflection coefficients

    spectrum_rc           = new double[nfreq];
    filtered_rc           = new double[nsamp];
    windowed_rc_spectrum  = new double[nfreq];

    complex_number [] cmplx_spec_rc = new complex_number[nfreq];

    for( int i = 0; i < nfreq; ++i ) { cmplx_spec_rc[i] = new complex_number(0.0, 0.0); }

    if( rctrue )

    {

    // Generate the analytical reflectivity spectrum

      rc_spectrum( freqs, filtercoefs, rcs, intimes, nfreq, nspikes, spectrum_rc, cmplx_spec_rc ); 

    //  Generate the filtered reflectivity trace

      filtered_rcs (trace_times, freqs, cmplx_spec_rc, nsamp, nfreq, filtered_rc );

    // Calculate the dft of the windowed RC trace

      freq_scaler = nfreq * dfreq / Nyquist;        // scaler to compare DFT with analytical RC spectrum

      windowed_dft (trace_times, freqs, filtered_rc, taper_weights, nsamp_window, nfreq, 
                    start_index, freq_scaler, windowed_rc_spectrum );

    }

    else

    {

      for( int i = 0; i < nfreq; ++i ) { spectrum_rc[i] = 0.0; windowed_rc_spectrum[i] = 0.0; }
      for( int i = 0; i < nsamp; ++i ) { filtered_rc[i] = 0.0; }

    }

    // Re-set the trace times to the input units

    for( int i = 0; i < nsamp; ++i ) { trace_times[i] /= UnitSc; }

  }

  private static boolean load_vectors (double [] impedances, double [] intimes, 
                                       double [] widths, double [] centres, double [] rcs )

  {

    int nblocks = impedances.length - 1;
    double rctest;
    boolean rctrue = true;

    for (int i = 0; i < nblocks; ++i)

    {

      widths[i]  =   intimes[i + 1] - intimes[i];
      centres[i] = ( intimes[i + 1] + intimes[i] ) / 2.0;

      rctest = impedances[i + 1] + impedances[i];

      if(Math.abs(rctest) > 0.0) { rcs[i] = ( impedances[i + 1] - impedances[i] ) / rctest; }
      else                  { rctrue = false; };

    }

    rctest = impedances[0] + impedances[nblocks];

    if(Math.abs(rctest) > 0.0) { rcs[nblocks] = ( impedances[0] - impedances[nblocks] ) / rctest; }
    else                  { rctrue = false; };

    return rctrue;

  }

  private static void load_trace_times(int nsamp, double [] trace_times, double dsamp, double centre_time)

  {

    int centre_samp = nsamp / 2;

    trace_times[centre_samp] = centre_time;

    for( int i = 1, ilow = centre_samp - 1, ihigh = centre_samp + 1; i < centre_samp + 1; ++i, --ilow, ++ihigh )

    {

      trace_times[ihigh] = (float)i * dsamp;
      trace_times[ilow]  = -trace_times[ihigh];

      trace_times[ihigh] += centre_time;
      trace_times[ilow]  += centre_time;

    }

  }

  private static void ormsby_coefs( double [] freqs, double [] filter, double [] filtercoefs, int nfreq, int smoothtype)

  {

    for( int i = 0; i < nfreq; ++i)

    {

      filtercoefs[i] = 0.0;

      if(      (freqs[i] >= filter[1]) && (freqs[i] <= filter[2]) ) { filtercoefs[i] = 1.0; }
      else if( (freqs[i] >  filter[0]) && (freqs[i] <  filter[1]) ) { filtercoefs[i] = (freqs[i] - filter[0]) / (filter[1] - filter[0]); }
      else if( (freqs[i] >  filter[2]) && (freqs[i] <  filter[3]) ) { filtercoefs[i] = (filter[3] - freqs[i]) / (filter[3] - filter[2]); }

    }

    // Apply Hanning smoothing to the filter coefficients

    smoothspec( filtercoefs, smoothtype, nfreq );

  }

  private static void smoothspec( double [] spectrum, int smoothtype, int nfreq )

  {

    double [] smoothcoefs = new double[2];

    if( smoothtype == 1) // SmoothType = 1 Hanning Smoothing

    {

      smoothcoefs[0] = 0.5;
      smoothcoefs[1] = 0.25;

    }

    else                 // Hamming - Tukey Smoothing

    {

      smoothcoefs[0] = 0.54;
      smoothcoefs[1] = 0.46 / 2.0;

    }

    int m1 = nfreq - 1;
    int mm = m1 - 1;

    double a  = smoothcoefs[0] * spectrum[0]  + 2.0 * smoothcoefs[1] * spectrum[1];
    double b  = smoothcoefs[0] * spectrum[m1] + 2.0 * smoothcoefs[1] * spectrum[mm];
    double sj = spectrum[0];
    double sk = spectrum[1];
    double si;

    for( int i = 1; i < mm; ++i )

    {

      si = sj;
      sj = sk;
      sk = spectrum[i + 1];
      spectrum[i] = smoothcoefs[0] * sj + smoothcoefs[1] * (si + sk);

    }

    spectrum[0]  = a;
    spectrum[m1] = b;

  }

  private static void filter_blocks( double [] centres, double [] widths, double [] impedances,
                                     double [] freqs, double [] filtercoefs, double [] trace_times,
                                     double [] spectrum_blocks, double [] filtered_blocks,
                                     int nsamp, int nfreq, int nblocks)

  {

    // Scaler to correct for sampled frequencies from both sides of a continuous sinc, when a spike occurs

    double spike_scaler = nfreq * 2 -1;

    // Initialise filtered_blocks vector

    for(int j = 0; j < nsamp; ++j ) { filtered_blocks[j] = 0.0; }

    // Calculate the components of the analytical Fourier Transform and filtered trace using the addition and shift theorems

    double PI2 = Math.PI * 2.0;

    for( int i = 0; i < nfreq; ++i )

    {

      complex_number g = new complex_number( 0.0, 0.0);

      block_dft( centres, widths, impedances, freqs[i], spike_scaler, nblocks, g);

      g.real *= filtercoefs[i];
      g.imag *= filtercoefs[i];

      spectrum_blocks[i] = complex_number.magnitude(g);        // Filtered analytical amplitude spectrum
                                                               // at the current frequency

      double pif2 = PI2 * freqs[i];
      double fnegs = ( Math.abs(freqs[i]) > 0.0 )? 2.0 : 1.0;  // Scale the sincs to integrate over both
                                                               // +ve and -ve frequencies during back DFT

      for(int j = 0; j < nsamp; ++j )

      {

        double phaseangle = pif2 * trace_times[j];
        filtered_blocks[j] += ( fnegs * ( g.real * Math.cos(phaseangle) + g.imag * Math.sin(phaseangle) ) );

      }

    }

  }

  private static void block_dft( double [] centres, double [] widths, double [] impedances, 
                                 double freq, double spike_scaler, int nblocks, complex_number g)

  {

    double pif  = Math.PI * freq;
    double pif2 = pif * 2.0;

    g.real = 0.0;
    g.imag = 0.0;

    double area;                     // area of block = sinc amplitude at freq = 0.0
    double phaseangle;

    for(int i = 0; i < nblocks; ++i)

    {

      if(widths[i] > 0.0)  // We have a block

      {

        area = widths[i] * (impedances[i + 1] - impedances[0]);
        phaseangle = pif * widths[i];

      }

      else                 // We have a spike

      {

        area = (impedances[i + 1] - impedances[0]) / spike_scaler;
        phaseangle = 0.0;

      }

      double sincs = (Math.abs(phaseangle) > 0.0)? Math.sin(phaseangle) / phaseangle : 1.0;

      // Fourier transform of block = sinc value at freq;

      sincs *= area;                       

      // now phase shift the whole sinc

      phaseangle = pif2 * centres[i];          
      g.real += sincs * Math.cos(phaseangle);
      g.imag += sincs * Math.sin(phaseangle);

    }

  }

  private static void cosine_taper ( int nsamp_window, double taper_percent, double [] taper_weights )

  {

    double taper_fraction = taper_percent / 100.0;

    double weight = (((double)nsamp_window * (1.0 - taper_fraction))) / 2.0;
    int index_half = nsamp_window / 2;

    double half_scaler = Math.abs(weight - index_half);
 
    int jfar = nsamp_window - 1;
    double jkl = weight + 1.0 - (double)index_half;
    int end = -(int)jkl;

    for(int j = 0; j < end + 1; ++j )

    {

      taper_weights[j] = (Math.cos(Math.PI * (double)jkl / half_scaler) + 1.0) / 2.0;
      taper_weights[jfar] = taper_weights[j];
      --jfar;
      ++jkl;

    }

  }

  private static void windowed_dft (double [] trace_times, double [] freqs, double [] trace, double [] taper_weights,
                                    int nsamp_window, int nfreq, int start_index, double freq_scaler, double [] windowed_spectrum )

  {

    complex_number [] terms = new complex_number[nsamp_window];
    double [] win_times = new double[nsamp_window];

    for(int i = 0, j = start_index; i < nsamp_window; ++i, ++j)

    {

      terms[i] = new complex_number(trace[j] * taper_weights[i], 0.0);
      win_times[i] = trace_times[j];

    }

    int dirn = -1;

    for(int i = 0; i < nfreq; ++i )

    {

      complex_number g = new complex_number(0.0, 0.0);

      dft( freqs[i], terms, win_times, nsamp_window, dirn, g);
      windowed_spectrum[i] = complex_number.magnitude(g) * freq_scaler;

    }

  }

  private static void dft( double freq, complex_number [] terms, double [] win_times,
                           int nsamp_window, int dirn, complex_number blk_spec)

  {

    dirn = ( dirn < 0 )? -1 : 1;

    double pif2 = (double)dirn * freq * Math.PI * 2.0;

    blk_spec.real = 0.0;
    blk_spec.imag = 0.0;

    for( int i = 0; i < nsamp_window; ++i )

    {

      double angle = pif2 * win_times[i];
      complex_number parts = new complex_number( Math.cos(angle), Math.sin(angle) );
      blk_spec.real += terms[i].real * parts.real - terms[i].imag * parts.imag;
      blk_spec.imag += terms[i].imag * parts.real + terms[i].real * parts.imag;

    }

    if( dirn > 0 ) {blk_spec.real /= nsamp_window; blk_spec.imag /= nsamp_window; }

  }

  private static void rc_spectrum( double [] freqs, double [] filtercoefs, double [] rcs, double [] intimes,
                                   int nfreq, int nspikes, double [] spectrum_rc, complex_number [] cmplx_spec_rc )

  {

    complex_number [] terms = new complex_number[nspikes];

    for(int i = 0; i < nspikes; ++i ) { terms[i] = new complex_number(rcs[i], 0.0); }

    int dirn = -1;

    for(int i = 0; i < nfreq; ++i )

    {

      complex_number g = new complex_number(0.0, 0.0);

      dft( freqs[i], terms, intimes, nspikes, dirn, g);
      g.real *= filtercoefs[i];
      g.imag *= filtercoefs[i];
      cmplx_spec_rc[i].real = g.real;
      cmplx_spec_rc[i].imag = g.imag;
      spectrum_rc[i] = complex_number.magnitude(g);

    }

  }

  private static void filtered_rcs (double [] trace_times, double [] freqs, complex_number [] cmplx_spec_rc, 
                                    int nsamp, int nfreq, double [] filtered_rc )

  {

    int dirn = 1;

    for(int i = 0; i < nsamp; ++i )

    {

      complex_number g = new complex_number(0.0, 0.0);

      dft( trace_times[i], cmplx_spec_rc, freqs, nfreq, dirn, g);
      filtered_rc[i] = g.real;

    }

  }

}

class complex_number

{

  public double real, imag;

  public complex_number( double real_part, double imag_part )

  {

    real = real_part;
    imag = imag_part;

  }

  public static double magnitude( complex_number z)

  {

    return Math.hypot(z.real, z.imag);

  }

}
