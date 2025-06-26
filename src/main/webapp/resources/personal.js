/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

function copyToClip(val, currency) {
    if(currency) val = val.replace('.', ',');
    navigator.clipboard.writeText(val);
    //alert("Copied the text: '" + val + "'");
}
