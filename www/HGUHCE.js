var exec = require('cordova/exec');

function HGUHCE() {
    console.log("HGUHCE.js: is created");
}

HGUHCE.prototype.listen = function( id, reg_count, success, error ){
	exec(success, error, "HGUHCE", 'connect', [id + '', reg_count + '']);
}

HGUHCE.prototype.close = function( success, error ){
	exec(success, error, "HGUHCE", 'close', []);
}

var hguhce = new HGUHCE();
module.exports = hguhce;