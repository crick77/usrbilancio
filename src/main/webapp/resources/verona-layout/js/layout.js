/** 
 * PrimeFaces Verona Layout
 */
PrimeFaces.widget.Verona = PrimeFaces.widget.BaseWidget.extend({
    
    init: function(cfg) {
        this._super(cfg);
        this.wrapper = $(document.body).children('.layout-wrapper');
        this.topbar = this.wrapper.children('.topbar');
        this.menuContainer = this.wrapper.children('.layout-menu-container');
        this.menuWrapper = this.topbar.children('.layout-menu-wrapper');
        this.menuButton = $('#menu-button');
        this.userDisplay = $('#user-display');
        this.topbarMenu = $('#topbar-menu');
        this.topbarLinks = this.topbarMenu.find('a');
        this.searchInput = this.topbarMenu.find('input');
        this.menulinks = this.menuWrapper.find('a');
        this.expandedMenuitems = this.expandedMenuitems || [];

        this.configButton = $('#layout-config-button');
        this.configurator = this.wrapper.children('.layout-config');
        this.configClicked = false;

        this.bindEvents();  
        
        if(this.wrapper.hasClass('layout-menu-static') || this.wrapper.hasClass('layout-menu-overlay')) {
            this.restoreMenuState();
        }
    },
    
    bindEvents: function() {
        var $this = this;
        
        this.menuButton.off('click.menubutton').on('click.menubutton', function(e) {
            if($this.isMobile()) {
                if($this.wrapper.hasClass('layout-menu-static')) {
                    if($this.wrapper.hasClass('layout-menu-static-inactive')) {
                        $this.wrapper.removeClass('layout-menu-static-inactive');
                        $this.menuWrapper.addClass('layout-menu-wrapper-active');
                        $this.menuButton.addClass('menu-button-active');
                    }
                    else {
                        $this.wrapper.addClass('layout-menu-static-inactive');
                        $this.menuWrapper.removeClass('layout-menu-wrapper-active');
                        $this.menuButton.removeClass('menu-button-active');
                    }
                }
                else {
                    $this.menuWrapper.toggleClass('layout-menu-wrapper-active');
                    $this.menuButton.toggleClass('menu-button-active');
                }
            }
            else {
                if($this.wrapper.hasClass('layout-menu-static')) {
                    $this.wrapper.toggleClass('layout-menu-static-inactive');
                    if($this.wrapper.hasClass('layout-menu-static-inactive')) {
                        $this.menuWrapper.removeClass('layout-menu-wrapper-active');
                        $this.menuButton.removeClass('menu-button-active');   
                    }
                    
                    setTimeout(function() {
                        $(window).trigger('resize');
                    }, 300);
                }
                else {
                    if($this.menuWrapper.hasClass('layout-menu-wrapper-active')) {
                        $this.hideMenu();
                        $this.menuButton.removeClass('menu-button-active');
                    }
                    else {
                        $this.menuWrapper.addClass('layout-menu-wrapper-active');
                        $this.menuButton.addClass('menu-button-active');
                    }
                }
            }
            
            $this.isBodyOverflowOnMobile();
            
            $this.menuButtonClick = true;
            e.preventDefault();
        });
        
        this.menulinks.off('click.menulink').on('click.menulink', function(e) {
            var link = $(this),
            item = link.parent(),
            submenu = item.children('ul');
            $this.menuClick = true;
                                    
            if($this.isHorizontal()) {                
                if(submenu.length) {
                    if(item.hasClass('active-menuitem')) {
                        item.removeClass('active-menuitem');
                        submenu.hide();
                        
                        if(item.parent().hasClass('layout-menu')) {
                            $this.menuActive = false;
                        }
                    }
                    else {
                        $this.deactivateItems(item.siblings('.active-menuitem'), false);
                        item.addClass('active-menuitem');
                        $this.addMenuitem(item.attr('id'));
                        submenu.show();
                        $this.menuActive = true;
                    }
                    
                    e.preventDefault();
                }
            }
            else {
                if(item.hasClass('active-menuitem')) {
                    if(submenu.length) {
                        $this.removeMenuitem(item.attr('id'));
                        item.removeClass('active-menuitem');
                        submenu.slideUp();
                    }
                }
                else {
                    $this.deactivateItems(item.siblings(), true);
                    $.cookie('verona_menu_scroll_state', link.attr('href') + ',' + $this.menuContainer.scrollTop(), { path: '/' });
                    item.addClass('active-menuitem');
                    $this.addMenuitem(item.attr('id'));
                    submenu.slideDown();
                }
                                        
                if(submenu.length) {
                    e.preventDefault();
                }
            }
        })
        .off('mouseenter.menulink').on('mouseenter.menulink', function(e) {
            if($this.isHorizontal()) {
                var item = $(this).parent();

                if($this.menuActive && item.parent().hasClass('layout-menu')) {
                    $this.deactivateItems(item.siblings('.active-menuitem'), false);
                    item.addClass('active-menuitem');
                    item.children('ul').show();
                }
            }
        });
        
        this.userDisplay.off('click.profilebutton').on('click.profilebutton', function(e) {
            $this.topbarMenuButtonClick = true;

            if($this.topbarMenu.hasClass('topbar-menu-visible')) {
                $this.hideTopBar();
            }
            else {
                $this.topbarMenu.addClass('topbar-menu-visible fadeInDown');
            }
                        
            e.preventDefault();
        });
        
        this.topbarLinks.off('click.topbarlink').on('click.topbarlink', function(e) {
            var link = $(this),
            item = link.parent();
            
            $this.topbarLinkClick = true;
            item.siblings('.menuitem-active').removeClass('menuitem-active');
            item.toggleClass('menuitem-active');
            
            var href = link.attr('href');
            if(href && href !== '#') {
                window.location.href = href;
            }

            e.preventDefault();
        });
        
        this.searchInput.off('click.search').on('click.search', function(e) {
            $this.topbarLinkClick = true;
        });

        this.configButton.off('click.configbutton').on('click.configbutton', function(e) {
            $this.configurator.toggleClass('layout-config-active');
            $this.configClicked = true;
        });

        this.configurator.off('click.config').on('click.config', function() {
            $this.configClicked = true;
        });

        $(document.body).off('click.layoutBody').on('click.layoutBody', function() {       
            if(!$this.menuButtonClick && !$this.menuClick && !$this.isStatic()) {
                if($this.isHorizontal()) {
                    $this.deactivateItems($this.menuWrapper.find('li.active-menuitem'), false);
                    $this.menuActive = false;
                }
                else {
                    $this.hideMenu();
                    $this.menuButton.removeClass('menu-button-active');
                }
            }
            
            if(!$this.topbarMenuButtonClick && !$this.topbarLinkClick) {
                $this.hideTopBar();
            }

            $this.isBodyOverflowOnMobile();

            if (!$this.configClicked && $this.configurator.hasClass('layout-config-active')) {
                $this.configurator.removeClass('layout-config-active');
            }

            $this.menuButtonClick = false;
            $this.menuClick = false;
            $this.topbarMenuButtonClick = false;
            $this.topbarLinkClick = false;
            $this.configClicked = false;
        });
    },
    
    hideTopBar: function() {
        var $this = this;
        this.topbarMenu.addClass('fadeOutUp');
        
        setTimeout(function() {
            $this.topbarMenu.removeClass('fadeOutUp topbar-menu-visible');
        },450);
    },
    
    hideMenu: function() {
        var $this = this;
        if(this.wrapper.hasClass('layout-menu-popup') && this.menuWrapper.hasClass('layout-menu-wrapper-active')) {
            this.menuWrapper.addClass('fadeOutUp');
            
            setTimeout(function() {
                $this.menuWrapper.removeClass('fadeOutUp layout-menu-wrapper-active');
            }, 300);   
        }
        else {
            $this.menuWrapper.removeClass('layout-menu-wrapper-active');
            if($this.wrapper.hasClass('layout-menu-static')) {
                $this.wrapper.addClass('layout-menu-static-inactive');
            }  
        }
         
    },
            
    deactivateItems: function(items, animate) {
        var $this = this;
        
        for(var i = 0; i < items.length; i++) {
            var item = items.eq(i),
            submenu = item.children('ul');
            
            if(submenu.length) {
                if(item.hasClass('active-menuitem')) {
                    var activeSubItems = item.find('.active-menuitem');
                    item.removeClass('active-menuitem');
                    
                    if(animate) {
                        submenu.slideUp('normal', function() {
                            $(this).parent().find('.active-menuitem').each(function() {
                                $this.deactivate($(this));
                            });
                        });
                    }
                    else {
                        submenu.hide();
                        item.find('.active-menuitem').each(function() {
                            $this.deactivate($(this));
                        });
                    }
                    
                    $this.removeMenuitem(item.attr('id'));
                    activeSubItems.each(function() {
                        $this.removeMenuitem($(this).attr('id'));
                    });
                }
                else {
                    item.find('.active-menuitem').each(function() {
                        var subItem = $(this);
                        $this.deactivate(subItem);
                        $this.removeMenuitem(subItem.attr('id'));
                    });
                }
            }
            else if(item.hasClass('active-menuitem')) {
                $this.deactivate(item);
                $this.removeMenuitem(item.attr('id'));
            }
        }
    },
    
    deactivate: function(item) {
        var submenu = item.children('ul');
        item.removeClass('active-menuitem');
        
        if(submenu.length) {
            submenu.hide();
        }
    },
    
    removeMenuitem: function (id) {
        this.expandedMenuitems = $.grep(this.expandedMenuitems, function (value) {
            return value !== id;
        });
        this.saveMenuState();
    },
    
    addMenuitem: function (id) {
        if ($.inArray(id, this.expandedMenuitems) === -1) {
            this.expandedMenuitems.push(id);
        }
        this.saveMenuState();
    },
    
    saveMenuState: function() {
        $.cookie('verona_expandeditems', this.expandedMenuitems.join(','), {path: '/'});
    },

    clearLayoutState: function() {
        this.clearMenuState();
        this.clearActiveItems();
    },

    clearActiveItems: function() {
        var activeItems = this.jq.find('li.active-menuitem'),
            subContainers = activeItems.children('ul');

        activeItems.removeClass('active-menuitem');
        if(subContainers && subContainers.length) {
            subContainers.hide();
        }
    },

    clearMenuState: function() {
        $.removeCookie('verona_expandeditems', {path: '/'});
    },
    
    restoreMenuState: function() {
        var $this = this;
        var menucookie = $.cookie('verona_expandeditems');
        if (menucookie) {
            this.expandedMenuitems = menucookie.split(',');
            for (var i = 0; i < this.expandedMenuitems.length; i++) {
                var id = this.expandedMenuitems[i];
                if (id) {
                    var menuitem = $("#" + this.expandedMenuitems[i].replace(/:/g, "\\:"));
                    menuitem.addClass('active-menuitem');
                    
                    var submenu = menuitem.children('ul');
                    if(submenu.length) {
                        submenu.show();
                    }
                }
            }

            setTimeout(function() {
                $this.restoreScrollState(menuitem);
            }, 100)
        }
    },

    restoreScrollState: function(menuitem) {
        var scrollState = $.cookie('verona_menu_scroll_state');
        if (scrollState) {
            var state = scrollState.split(',');
            if (state[0].startsWith(this.cfg.pathname) || this.isScrolledIntoView(menuitem, state[1])) {
                this.menuContainer.scrollTop(parseInt(state[1], 10));
            }
            else {
                this.scrollIntoView(menuitem.get(0));
                $.removeCookie('verona_menu_scroll_state', { path: '/' });
            }
        }
        else if (!this.isScrolledIntoView(menuitem, menuitem.scrollTop())){
            this.scrollIntoView(menuitem.get(0));
        }
    },

    scrollIntoView: function(elem) {
        if (document.documentElement.scrollIntoView) {
            elem.scrollIntoView({ block: "nearest", inline: 'start' });

            var wrapper = $('.layout-menu-wrapper');
            var scrollTop = wrapper.scrollTop();
            if (scrollTop > 0) {
                wrapper.scrollTop(scrollTop + parseFloat(this.topbar.height()));
            }
        }
    },

    isScrolledIntoView: function(elem, scrollTop) {
        var viewBottom = parseInt(scrollTop, 10) + this.menuContainer.height();

        var elemTop = elem.position().top;
        var elemBottom = elemTop + elem.height();

        return ((elemBottom <= viewBottom) && (elemTop >= scrollTop));
    },
    
    isHorizontal: function() {
        return window.innerWidth > 1024 && this.wrapper.hasClass('layout-menu-horizontal');
    },
    
    isStatic: function() {
        return window.innerWidth > 1024 && this.wrapper.hasClass('layout-menu-static');
    },
    
    isOverlay: function() {
        return window.innerWidth > 1024 && this.wrapper.hasClass('layout-menu-overlay');
    },
    
    isBodyOverflowOnMobile: function() {
        if(this.isMobileDevice()) {
            if(this.menuButton.hasClass('menu-button-active'))
                $(document.body).css('overflow', 'hidden');
            else
                $(document.body).css('overflow', '');
        }   
    },
    
    isMobile: function() {
        return window.innerWidth < 1025;
    },
    
    isMobileDevice: function() {
        return /android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini/i.test(window.navigator.userAgent.toLowerCase());
    }
    
});

PrimeFaces.VeronaConfigurator = {

    changeMenuMode: function(menuMode) {
        var wrapper = $(document.body).children('.layout-wrapper');
        switch (menuMode) {
            case 'static':
                wrapper.addClass('layout-menu-static').removeClass('layout-menu-overlay layout-menu-popup layout-menu-horizontal');
                this.clearLayoutState();
                break;

            case 'overlay':
                wrapper.addClass('layout-menu-overlay').removeClass('layout-menu-static layout-menu-popup layout-menu-horizontal');
                this.clearLayoutState();
                break;

            case 'popup':
                wrapper.addClass('layout-menu-popup').removeClass('layout-menu-static layout-menu-overlay layout-menu-horizontal');
                this.clearLayoutState();
                break;

            case 'horizontal':
                wrapper.addClass('layout-menu-horizontal').removeClass('layout-menu-static layout-menu-overlay layout-menu-popup');
                this.clearLayoutState();
                break;

            default:
                wrapper.addClass('layout-menu-static').removeClass('layout-menu-overlay layout-menu-popup layout-menu-horizontal');
                this.clearLayoutState();
                break;
        }
    },

    changeLayout: function(layoutTheme) {
        var linkElement = $('link[href*="layout-"]');
        var href = linkElement.attr('href');
        var startIndexOf = href.indexOf('layout-') + 7;
        var endIndexOf = href.indexOf('.css');
        var currentColor = href.substring(startIndexOf, endIndexOf);

        this.replaceLink(linkElement, href.replace(currentColor, layoutTheme));
    },

    changeScheme: function(theme) {
        var library = 'primefaces-verona';
        var linkElement = $('link[href*="theme.css"]');
        var href = linkElement.attr('href');
        var index = href.indexOf(library) + 1;
        var currentTheme = href.substring(index + library.length);

        this.replaceLink(linkElement, href.replace(currentTheme, theme));
    },

    beforeResourceChange: function() {
        PrimeFaces.ajax.RESOURCE = null;    //prevent resource append
    },

    replaceLink: function(linkElement, href) {
        PrimeFaces.ajax.RESOURCE = 'javax.faces.Resource';

        var isIE = this.isIE();

        if (isIE) {
            linkElement.attr('href', href);
        }
        else {
            var cloneLinkElement = linkElement.clone(false);

            cloneLinkElement.attr('href', href);
            linkElement.after(cloneLinkElement);

            cloneLinkElement.off('load').on('load', function() {
                linkElement.remove();
            });
        }
    },

    clearLayoutState: function() {
        var menu = PF('VeronaMenuWidget');

        if (menu) {
            menu.clearLayoutState();
        }
    },

    isIE: function() {
        return /(MSIE|Trident\/|Edge\/)/i.test(navigator.userAgent);
    },

    updateInputStyle: function(value) {
        if (value === 'filled')
            $(document.body).addClass('ui-input-filled');
        else
            $(document.body).removeClass('ui-input-filled');
    }
};

/*!
 * jQuery Cookie Plugin v1.4.1
 * https://github.com/carhartl/jquery-cookie
 *
 * Copyright 2006, 2014 Klaus Hartl
 * Released under the MIT license
 */
(function (factory) {
	if (typeof define === 'function' && define.amd) {
		// AMD (Register as an anonymous module)
		define(['jquery'], factory);
	} else if (typeof exports === 'object') {
		// Node/CommonJS
		module.exports = factory(require('jquery'));
	} else {
		// Browser globals
		factory(jQuery);
	}
}(function ($) {

	var pluses = /\+/g;

	function encode(s) {
		return config.raw ? s : encodeURIComponent(s);
	}

	function decode(s) {
		return config.raw ? s : decodeURIComponent(s);
	}

	function stringifyCookieValue(value) {
		return encode(config.json ? JSON.stringify(value) : String(value));
	}

	function parseCookieValue(s) {
		if (s.indexOf('"') === 0) {
			// This is a quoted cookie as according to RFC2068, unescape...
			s = s.slice(1, -1).replace(/\\"/g, '"').replace(/\\\\/g, '\\');
		}

		try {
			// Replace server-side written pluses with spaces.
			// If we can't decode the cookie, ignore it, it's unusable.
			// If we can't parse the cookie, ignore it, it's unusable.
			s = decodeURIComponent(s.replace(pluses, ' '));
			return config.json ? JSON.parse(s) : s;
		} catch(e) {}
	}

	function read(s, converter) {
		var value = config.raw ? s : parseCookieValue(s);
		return $.isFunction(converter) ? converter(value) : value;
	}

	var config = $.cookie = function (key, value, options) {

		// Write

		if (arguments.length > 1 && !$.isFunction(value)) {
			options = $.extend({}, config.defaults, options);

			if (typeof options.expires === 'number') {
				var days = options.expires, t = options.expires = new Date();
				t.setMilliseconds(t.getMilliseconds() + days * 864e+5);
			}

			return (document.cookie = [
				encode(key), '=', stringifyCookieValue(value),
				options.expires ? '; expires=' + options.expires.toUTCString() : '', // use expires attribute, max-age is not supported by IE
				options.path    ? '; path=' + options.path : '',
				options.domain  ? '; domain=' + options.domain : '',
				options.secure  ? '; secure' : ''
			].join(''));
		}

		// Read

		var result = key ? undefined : {},
			// To prevent the for loop in the first place assign an empty array
			// in case there are no cookies at all. Also prevents odd result when
			// calling $.cookie().
			cookies = document.cookie ? document.cookie.split('; ') : [],
			i = 0,
			l = cookies.length;

		for (; i < l; i++) {
			var parts = cookies[i].split('='),
				name = decode(parts.shift()),
				cookie = parts.join('=');

			if (key === name) {
				// If second argument (value) is a function it's a converter...
				result = read(cookie, value);
				break;
			}

			// Prevent storing a cookie that we couldn't decode.
			if (!key && (cookie = read(cookie)) !== undefined) {
				result[name] = cookie;
			}
		}

		return result;
	};

	config.defaults = {};

	$.removeCookie = function (key, options) {
		// Must not alter options, thus extending a fresh object...
		$.cookie(key, '', $.extend({}, options, { expires: -1 }));
		return !$.cookie(key);
	};

}));

/* Issue #924 is fixed for 5.3+ and 6.0. (compatibility with 5.3) */
if(window['PrimeFaces'] && window['PrimeFaces'].widget.Dialog) {
    PrimeFaces.widget.Dialog = PrimeFaces.widget.Dialog.extend({

        enableModality: function() {
            this._super();
            $(document.body).children(this.jqId + '_modal').addClass('ui-dialog-mask');
        },

        syncWindowResize: function() {}
    });
}

if (PrimeFaces.widget.SelectOneMenu) {
    PrimeFaces.widget.SelectOneMenu = PrimeFaces.widget.SelectOneMenu.extend({
        init: function (cfg) {
            this._super(cfg);

            var $this = this;
            if (this.jq.parent().hasClass('ui-float-label')) {
                this.m_panel = $(this.jqId + '_panel');
                this.m_focusInput = $(this.jqId + '_focus');

                this.m_panel.addClass('ui-input-overlay-panel');
                this.jq.addClass('ui-inputwrapper');

                if (this.input.val() != '') {
                    this.jq.addClass('ui-inputwrapper-filled');
                }

                this.input.off('change').on('change', function () {
                    $this.inputValueControl($(this));
                });

                this.m_focusInput.on('focus.ui-selectonemenu', function () {
                    $this.jq.addClass('ui-inputwrapper-focus');
                })
                    .on('blur.ui-selectonemenu', function () {
                        $this.jq.removeClass('ui-inputwrapper-focus');
                    });

                if (this.cfg.editable) {
                    this.label.on('input', function (e) {
                        $this.inputValueControl($(this));
                    }).on('focus', function () {
                        $this.jq.addClass('ui-inputwrapper-focus');
                    }).on('blur', function () {
                        $this.jq.removeClass('ui-inputwrapper-focus');
                        $this.inputValueControl($(this));
                    });
                }
            }
        },

        inputValueControl: function (input) {
            if (input.val() != '')
                this.jq.addClass('ui-inputwrapper-filled');
            else
                this.jq.removeClass('ui-inputwrapper-filled');
        }
    });
}

if (PrimeFaces.widget.Chips) {
    PrimeFaces.widget.Chips = PrimeFaces.widget.Chips.extend({
        init: function (cfg) {
            this._super(cfg);

            var $this = this;
            if (this.jq.parent().hasClass('ui-float-label')) {
                this.jq.addClass('ui-inputwrapper');

                if ($this.jq.find('.ui-chips-token').length !== 0) {
                    this.jq.addClass('ui-inputwrapper-filled');
                }

                this.input.on('focus.ui-chips', function () {
                    $this.jq.addClass('ui-inputwrapper-focus');
                }).on('input.ui-chips', function () {
                    $this.inputValueControl();
                }).on('blur.ui-chips', function () {
                    $this.jq.removeClass('ui-inputwrapper-focus');
                    $this.inputValueControl();
                });

            }
        },

        inputValueControl: function () {
            if (this.jq.find('.ui-chips-token').length !== 0 || this.input.val() != '')
                this.jq.addClass('ui-inputwrapper-filled');
            else
                this.jq.removeClass('ui-inputwrapper-filled');
        }
    });
}

if (PrimeFaces.widget.DatePicker) {
    PrimeFaces.widget.DatePicker = PrimeFaces.widget.DatePicker.extend({
        init: function (cfg) {
            this._super(cfg);

            var $this = this;
            if (this.jq.parent().hasClass('ui-float-label') && !this.cfg.inline) {
                if (this.input.val() != '') {
                    this.jq.addClass('ui-inputwrapper-filled');
                }

                this.jqEl.off('focus.ui-datepicker blur.ui-datepicker change.ui-datepicker')
                    .on('focus.ui-datepicker', function () {
                        $this.jq.addClass('ui-inputwrapper-focus');
                    })
                    .on('blur.ui-datepicker', function () {
                        $this.jq.removeClass('ui-inputwrapper-focus');
                    })
                    .on('change.ui-datepicker', function () {
                        $this.inputValueControl($(this));
                    });
            }
        },

        inputValueControl: function (input) {
            if (input.val() != '')
                this.jq.addClass('ui-inputwrapper-filled');
            else
                this.jq.removeClass('ui-inputwrapper-filled');
        }
    });
}