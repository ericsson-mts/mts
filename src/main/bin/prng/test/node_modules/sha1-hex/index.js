'use strict';

var createHash = require('crypto').createHash;

module.exports = function(buf) {
  return createHash('sha1').update(buf).digest('hex');
};
