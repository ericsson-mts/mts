// Function returns a random number between 1 and n
function rand(n){return(Math.floor(Math.random()*n+1));}

jQuery(document).ready(function($){
 
  // Disable HTML title attribute for slider images
  $('.promo_slider img').removeAttr('title');
 
  // Get all instances of promo_slider on the page
  var sliders = $('.promo_slider_wrapper');
  
  // Cycle through each slider
  $.each(sliders, function(){
	
	// Define current slider
	var currentSlider = $(this);
	var thisSlider = $('.promo_slider', currentSlider);
	
	// Get all panels
    var panels = $('.panel', thisSlider);
	
	// Get total count of panels
    var panelCount = panels.length;
	
	// Set number for first panel
  	var initialPanel;
	if( promoslider_options.startOn == 'first' ) {
		initialPanel = 1;
	} else {
		initialPanel = rand(panelCount);
	}
	if( currentSlider.hasClass('random') ) initialPanel = rand(panelCount);
	if( currentSlider.hasClass('first') ) initialPanel = 1;

	// Should we pause the slider on mouseover?
	var pauseOnMouseover = currentSlider.hasClass('pause');

	// Assign variable for setInterval
	var sliderInterval;

	// Set time delay
	var timeDelay = promoslider_options.timeDelay;
	if( $('.promo_slider_time_delay', thisSlider).html() ){
		timeDelay = $('.promo_slider_time_delay', thisSlider).html();
	}

	// Set auto advance variable
	var autoAdvance = promoslider_options.autoAdvance;
	if( thisSlider.hasClass('auto_advance') ) autoAdvance = true;
	if( thisSlider.hasClass('no_auto_advance') ) autoAdvance = false;
	if( panelCount < 2 ) autoAdvance = false;

	// Set navigation option
	var navOption = promoslider_options.nav_option;
	if( currentSlider.hasClass('default_nav') ) navOption = 'default';
	else if( currentSlider.hasClass('fancy_nav') ) navOption = 'fancy';
	else if( currentSlider.hasClass('links_nav') ) navOption = 'links';
	else if( currentSlider.hasClass('thumb_nav') ) navOption = 'thumb';
	else if( currentSlider.hasClass('tabbed_nav') ) navOption = 'tabbed';
	else navOption = false;

	// Hide all panels
	panels.hide();

	// Show initial panel and add class 'current' to active panel
	$('.panel-' + initialPanel, currentSlider).show().addClass('current');

	  if(panelCount > 1 && (navOption == 'default' || navOption == 'fancy' || navOption == 'thumb' || navOption == 'tabbed' ) ){

        var navHTML;

		if( 'tabbed' != navOption ) {
			$('.promo_slider_nav').not('.tabbed_ps_nav').show();
		}
		$('.promo_slider_thumb_nav').show();

		if(navOption == 'tabbed'){
		  // Generate HTML for navigation
		  navHTML = '';
		  $.each(panels, function(index, object){
			// Set panel title
			var panelTitle = $('.panel-'+(index+1)+' span.panel-title', currentSlider).html();
			  var newSpan = '<span class="'+(index+1)+'" title="'+panelTitle+'">'+panelTitle+'</span>';
			navHTML = navHTML + newSpan;
		  });

		  // Insert HTML into nav
		  $('.slider_selections', currentSlider).html(navHTML);
		}

	  if(navOption == 'fancy' || navOption == 'default'){
		  // Generate HTML for navigation
		  navHTML = '';
		  $.each(panels, function(index, object){
			// Set panel title
			panelTitle = $('.panel-'+(index+1)+' span.panel-title', currentSlider).html();
			  newSpan = '<span class="'+(index+1)+'" title="'+panelTitle+'">'+(index+1)+'</span>';
			  if( (index + 1) != panelCount){newSpan = newSpan + '<b class="promo_slider_sep"> | </b>';}
			navHTML = navHTML + newSpan;
		  });
		  
		  // Insert HTML into nav
		  $('.slider_selections', currentSlider).html(navHTML);
	  }
	  
	  // Set click functions for each span in the slider nav
	  var slideNav = $('.slider_selections span', currentSlider);
	  $.each(slideNav, function(index, object){
		$(object).click(function(){
		  clearInterval(sliderInterval);
		  if( !$(object).hasClass('current') ) progress($(object).attr('class'), currentSlider, panelCount);
		  if(autoAdvance) sliderInterval = setInterval(function(){progress('forward', currentSlider, panelCount);}, (timeDelay * 1000));
		});
	  });
	  
	  // Set active span class to 'current'
	  $('.slider_selections span[class=' + initialPanel + ']', currentSlider).addClass('current');
	  
	}
	
	// Create click functions for navigational elements
	$('.move_forward', currentSlider).click(function(){
        clearInterval(sliderInterval);
        progress('forward', currentSlider, panelCount);
        if(autoAdvance) sliderInterval = setInterval(function(){progress('forward', currentSlider, panelCount);}, (timeDelay * 1000));
    });
	$('.move_backward', currentSlider).click(function(){
        clearInterval(sliderInterval);
        progress('backward', currentSlider, panelCount);
        if(autoAdvance) sliderInterval = setInterval(function(){progress('forward', currentSlider, panelCount);}, (timeDelay * 1000));
    });
	
	
	if( autoAdvance ){
	
		// Begin auto advancement of slides
		sliderInterval = setInterval(function(){progress('forward', currentSlider, panelCount);}, (timeDelay * 1000));
	
		if( pauseOnMouseover ){
		
			// Pause slide advancement on mouseover
			$(thisSlider).mouseover(function(){
				clearInterval(sliderInterval);
			});
		
			// Continue slide advancement on mouseout
			$(thisSlider).mouseout(function(){
				sliderInterval = setInterval(function(){progress('forward', currentSlider, panelCount);}, (timeDelay * 1000));
			});
		
		}
		
	}
	
  });
  
  // Progress to selected panel
  function progress(value, currentSlider, panelCount){

      // Find number of current panel
      var currentValue = $('div.promo_slider > .panel', currentSlider).index($('div.panel.current', currentSlider)) + 1;

      var panels = $('.panel', currentSlider);
      var current = $('.panel.current', currentSlider);

      panels.stop( true, true );

      // Set value of new panel
      var panelNum;
      if( value == 'forward' ){
          panelNum = current.is( panels.last() ) ? 1: currentValue + 1;
      } else if(value == 'backward'){
          panelNum = current.is( panels.first() ) ? panels.length: currentValue - 1;
      } else{
          panelNum = value;
      }
	  
	  // Assign variables for ease of use
	  var next = $('.panel-' + panelNum, currentSlider);
	  var currentSpan = $('.slider_selections span.current', currentSlider);
	  var newSpan = $('.slider_selections span.' + panelNum, currentSlider);
	  
	  // Add / Remove classes
	  current.removeClass('current');
	  next.addClass('current');
	  currentSpan.removeClass('current');
	  newSpan.addClass('current');

	  // Fade in / out
      next.hide().css({ 'z-index':1 });
      current.css({ 'z-index':0 });

	  next.fadeIn( 1200, 'swing', function() {
		  current.hide();
	  } );

  }

});