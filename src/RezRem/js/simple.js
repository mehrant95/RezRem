(function($){

    var elements = $('body > div');
    var now_reserve = $('#now-reserve');
    var reserve_done = $('#reserve-done');

    $('#exit').on('click', function () {
        $.when(elements.fadeOut()).done(function () {
            window.Events.exit();
        });
    });

    $('#go-to-main').on('click', function () {
        $.when(elements.fadeOut()).done(function () {
            window.Events.goToMain();
        });
    });

    now_reserve.on('click', function () {
        window.Events.nowReserve();
    });

    reserve_done.on('click', function () {
        window.Events.reserveDone();
    });

})(jQuery);