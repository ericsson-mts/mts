var crypto = require('crypto');
var sha1_hex = require('sha1-hex');
var console = require('console');

var s2b = function(string) {
  return (new Buffer(string, "ascii"));
};

var h2b = function(hex) {
  return (new Buffer(hex, "hex"));
};

var h2s = function(hex) {
  return h2b(hex).toString('ascii');
};


var identity = s2b("0208044460005000@nai.epc.mnc004.mcc208.3gppnetwork.org");
var ik = h2b("ee78ba18d36aeb993acb600a9718305f");
var ck = h2b("c16f01e478cdffc371923bf70fbe4206");

var buffers = [identity, ik, ck];

var concat_len = 0;

for (var i = 0; i < buffers.length; i++) {
  concat_len += buffers[i].length;
}

var mk =  sha1_hex(Buffer.concat(buffers, concat_len));

console.log("debug", "identity > " + identity);
console.log("debug", "ik > " + ik);
console.log("debug", "ck > " + ck);
console.log("debug", "mk > " + mk);


// AT_MAC
var k_auth = h2s("8b5d28b6eae4675be6df9d5ae6121e2d");
var eap_payload = h2s("02020028170100000b050000000000000000000000000000000000000303004039bcb97622c4715a");

var hmac_sha1 = crypto.createHmac('sha1', k_auth).update(eap_payload).digest('hex');

console.log("debug", "k_auth > " + k_auth);
console.log("debug", "eap_payload > " + eap_payload);
console.log("debug", "hmac_sha1 > " + hmac_sha1);
