function show_video() {
    //console.info("show video");
    var youtube_html = '<div id="scrim"><span class="close-button">×</span><iframe id="youtube-player" src="//www.youtube.com/embed/6gyLPjDakAc?wmode=opaque&vq=hd1080&rel=0&controls=1&showinfo=0&enablejsapi=1&origin=https://skylock.cc&autoplay=1" frameborder="0" allowfullscreen></iframe></div>';
    $("body").append(youtube_html);
    $("#scrim").on("click", function() {
        hide_video();
    });
    $(window).on("scroll", function() {
        hide_video();
    });
    $("#scrim").css('visibility','visible').hide().fadeIn(400);
    setVideoSize();
}
function setVideoSize() {
    var height = $(window).height();
    var width = $(window).width();
  	var video_ratio = 0.562;
    var youtube_player = $("#youtube-player");
  	var close_button = $(".close-button");
    if (height / width < video_ratio) {
        youtube_player.height(parseInt(height * 0.8));
        youtube_player.width(parseInt(height * 0.8 / video_ratio));
      	close_button.width(parseInt(height * 0.8 / video_ratio + 61));
    } else {
        youtube_player.width(parseInt(width * 0.8));
        youtube_player.height(parseInt(width * 0.8 * video_ratio));
      	close_button.width(parseInt(width * 0.8 + 61));
    }
}
function hide_video() {
    var obj = $("#scrim");
    obj.fadeOut(300, function() {
        obj.remove(); 
    });
}
function checkoutStatus() {
    if ($("#tos-checkbox").is(":checked") && product_count > 0) {
        $("#checkout").attr("disabled",false);
    } else {
        $("#checkout").attr("disabled",true);
    }
}


/*
    $(document).ready(function(){
      $(window).load(function() {  
        var ih = $('.home-top-section .imgpart').height();
        var ch = $('.home-top-section .contentpart').height();
        var x = 0;
        if(ih > ch) {
          x = ih - ch;
          x = x/2;
          $('.home-top-section .contentpart').css('padding-top',x+'px');
        }
        else {
          x = ch - ih;
          x = x/2;
          $('.home-top-section .imgpart').css('padding-top',x+'px');
        }
        
        
      });

    });
      $(window).resize(function() {  
        var ih = $('.home-top-section .imgpart').height();
        var ch = $('.home-top-section .contentpart').height();
        var x = 0;
        if(ih > ch) {
          x = ih - ch;
          x = x/2;
          $('.home-top-section .contentpart').css('padding-top',x+'px');
        }
        else {
          x = ch - ih;
          x = x/2;
          $('.home-top-section .imgpart').css('padding-top',x+'px');
        }
      });
*/