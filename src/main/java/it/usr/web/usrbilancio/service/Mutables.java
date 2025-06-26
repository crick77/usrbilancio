/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import java.util.Objects;

/**
 *
 * @author riccardo.iovenitti
 */
public abstract class Mutables {
    public static class MutableBoolean {
        public boolean flag = false;       

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 23 * hash + (this.flag ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MutableBoolean other = (MutableBoolean) obj;
            return this.flag == other.flag;
        }

        @Override
        public String toString() {
            return String.valueOf(flag);
        }                
    }
    
    public static class MutableString {
        public String s = null;        

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 43 * hash + Objects.hashCode(this.s);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MutableString other = (MutableString) obj;
            return Objects.equals(this.s, other.s);
        }

        @Override
        public String toString() {
            return s;
        }                
    }
    
    public static class MutableInteger {
        public Integer i = null;

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 61 * hash + Objects.hashCode(this.i);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MutableInteger other = (MutableInteger) obj;
            return Objects.equals(this.i, other.i);
        }

        @Override
        public String toString() {
            return String.valueOf(i);
        }                
    }
}
