(function ($) {

    let slider = $('.range-slider');
    let range = $('.range-slider__range');
    let value = $('.range-slider__value');

    slider.each(function(){

        value.each(function(){
            let value = jQuery3(this).prev().attr('value');
            jQuery3(this).html(value);
        });

        range.on('input', function(){
            jQuery3(this).next(value).html(this.value);
        });
    });

})(jQuery3)
