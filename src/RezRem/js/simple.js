(function($){

    var elements = $('body > div');
    var now_reserve = $('#now-reserve');
    var reserve_done = $('#reserve-done');

    $('#exit').on('click', function () {
        $.when(elements.fadeOut()).done(function () {
            Exit();
        });
    });

    $('#go-to-main').on('click', function () {
        $.when(elements.fadeOut()).done(function () {
            GoToMain();
        });
    });

    now_reserve.on('click', function () {
        NowReserve();
    });

    reserve_done.on('click', function () {
        ReserveDone();
    });

    var res = reservedNextWeek();

    if (res) {

        now_reserve.find('a').addClass('disabled').removeClass('waves-effect');

        reserve_done.find('a').addClass('disabled').removeClass('waves-effect');

    }
    else {

        now_reserve.find('a').removeClass('disabled').addClass('waves-effect');

        reserve_done.find('a').removeClass('disabled').addClass('waves-effect');

    }

})(jQuery);