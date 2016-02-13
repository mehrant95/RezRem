(function($){

    var elements = $('body > div');

    $('#return').on('click', function () {
        $.when(elements.fadeOut()).done(function () {
            window.Events.goToFirst();
        });
    });

})(jQuery);