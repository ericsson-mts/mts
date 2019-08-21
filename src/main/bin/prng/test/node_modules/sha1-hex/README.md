# sha1-hex [![Build Status](https://travis-ci.org/fundon/sha1-hex.svg?branch=master)](https://travis-ci.org/fundon/sha1-hex)

> Create a SHA1 hash with hex encoding


## Install

```
$ npm install --save sha1-hex
```


## Usage

```js
var fs = require('fs');
var sha1Hex = require('sha1-hex');
var buffer = fs.readFileSync('unicorn.png');

sha1Hex(buffer);
//=> '84de6753b298abd027fcd1d790eade2413eafb5a'
```


## API

### sha1Hex(input)

#### input

*Required*  
Type: `buffer`, `string`

Prefer buffers as they're faster to hash, but strings can be useful for small things.


## License

MIT
