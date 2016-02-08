(function($){

    var elements = $('body > div');

    $('#exit').on('click', function () {
        $.when(elements.fadeOut()).done(function () {
            Exit();
        });
    });
    $("#reserves").on('click', function () {
        $.when(elements.fadeOut()).done(function () {
            GetReserves();
        });
    });
})(jQuery);