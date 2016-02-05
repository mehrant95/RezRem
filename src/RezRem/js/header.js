(function($){

    var close_btn = $('#close-btn');
    var close_btn_hover = $('#close-btn-hover');
    var minimize_btn = $('#minimize-btn');
    var minimize_btn_hover = $('#minimize-btn-hover');

    close_btn.hover(function () {
        $(this).fadeOut(0, function () {
            close_btn_hover.fadeIn();
        });
    });

    close_btn_hover.hover(function () {
    }, function () {
        $(this).fadeOut(0, function () {
            close_btn.fadeIn();
        });
    });

    minimize_btn.hover(function () {
        $(this).fadeOut(0, function () {
            minimize_btn_hover.fadeIn();
        });
    });

    minimize_btn_hover.hover(function () {
    }, function () {
        $(this).fadeOut(0, function () {
            minimize_btn.fadeIn();
        });
    });

    close_btn_hover.on('click', function () {
        Close();
    });

    minimize_btn_hover.on('click', function () {
        Minimize();
    });

})(jQuery);