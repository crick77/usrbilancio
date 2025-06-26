/*
   Copyright 2009-2022 PrimeTek.

   Licensed under PrimeFaces Commercial License, Version 1.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   Licensed under PrimeFaces Commercial License, Version 1.0 (the "License");

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.primefaces.verona.view;

import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@Named
@SessionScoped
public class GuestPreferences implements Serializable {

    private String layout = "flow";

    private String theme = "turquoise";

    private String menuMode = "horizontal";

    private String inputStyle = "outlined";

    private List<FlatLayout> flatLayouts;

    private List<SpecialLayout> specialLayouts;

    private List<Theme> themes;

    @PostConstruct
    public void init() {
        flatLayouts = new ArrayList<>();
        flatLayouts.add(new FlatLayout("Dark", "dark", "#3b3b48"));
        flatLayouts.add(new FlatLayout("Turquoise", "turquoise", "#04838f"));
        flatLayouts.add(new FlatLayout("Green", "green", "#1e8455"));
        flatLayouts.add(new FlatLayout("Blue", "blue", "#2461cc"));
        flatLayouts.add(new FlatLayout("Rose", "rose", "#79425a"));
        flatLayouts.add(new FlatLayout("Teal", "teal", "#427976"));
        flatLayouts.add(new FlatLayout("Blue-Grey", "bluegrey", "#37474f"));
        flatLayouts.add(new FlatLayout("Purple", "purple", "#5d4279"));

        specialLayouts = new ArrayList<>();
        specialLayouts.add(new SpecialLayout("Cosmic", "cosmic", "#517fa4", "#243949"));
        specialLayouts.add(new SpecialLayout("Lawrencium", "lawrencium", "#302b63", "#201B4C"));
        specialLayouts.add(new SpecialLayout("Couple", "couple", "#3a6186", "#89253e"));
        specialLayouts.add(new SpecialLayout("Stellar", "stellar", "#7474BF", "#348AC7"));
        specialLayouts.add(new SpecialLayout("Beach", "beach", "#00cdac", "#02aab0"));
        specialLayouts.add(new SpecialLayout("Flow", "flow", "#136a8a", "#267871"));
        specialLayouts.add(new SpecialLayout("Fly", "fly", "#7b4397", "#b22f64"));
        specialLayouts.add(new SpecialLayout("Nepal", "nepal", "#614385", "#516395"));
        specialLayouts.add(new SpecialLayout("Celestial", "celestial", "#734b6d", "#734b6d"));

        themes = new ArrayList<>();
        themes.add(new Theme("green", "green", "#9fd037"));
        themes.add(new Theme("teal", "teal", "#12b886"));
        themes.add(new Theme("blue", "blue", "#3ebaf8"));
        themes.add(new Theme("amber", "amber", "#f7cb00"));
        themes.add(new Theme("purple", "purple", "#966af1"));
        themes.add(new Theme("turquoise", "turquoise", "#2ab1be"));
        themes.add(new Theme("bluegrey", "bluegrey", "#546E7A"));
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getMenuMode() {
        return this.menuMode;
    }

    public void setMenuMode(String menuMode) {
        this.menuMode = menuMode;
    }

    public String getInputStyle() {
        return inputStyle;
    }

    public void setInputStyle(String inputStyle) {
        this.inputStyle = inputStyle;
    }

    public String getInputStyleClass() {
        return this.inputStyle.equals("filled") ? "ui-input-filled" : "";
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public List<FlatLayout> getFlatLayouts() {
        return flatLayouts;
    }

    public List<SpecialLayout> getSpecialLayouts() {
        return specialLayouts;
    }

    public class Theme {
        String name;
        String file;
        String color;

        public Theme(String name, String file, String color) {
            this.name = name;
            this.file = file;
            this.color = color;
        }

        public String getName() {
            return this.name;
        }

        public String getFile() {
            return this.file;
        }

        public String getColor() {
            return this.color;
        }
    }

    public class FlatLayout {
        String name;
        String file;
        String color;

        public FlatLayout(String name, String file, String color) {
            this.name = name;
            this.file = file;
            this.color = color;
        }

        public String getName() {
            return this.name;
        }

        public String getFile() {
            return this.file;
        }

        public String getColor() {
            return this.color;
        }
    }

    public class SpecialLayout {
        String name;
        String file;
        String color1;
        String color2;

        public SpecialLayout(String name, String file, String color1, String color2) {
            this.name = name;
            this.file = file;
            this.color1 = color1;
            this.color2 = color2;
        }

        public String getName() {
            return this.name;
        }

        public String getFile() {
            return this.file;
        }

        public String getColor1() {
            return color1;
        }

        public String getColor2() {
            return color2;
        }
    }
}
