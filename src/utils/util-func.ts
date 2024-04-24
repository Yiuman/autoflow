function uuid(len: number, nonnumericBeginning: boolean = false, radix: number = 62): string {
  const alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'
  const chars = ('0123456789' + alphabet).split('')
  const uuid = []
  let i
  radix = radix || chars.length

  if (len) {
    // Compact form
    for (i = 0; i < len; i++) {
      if (nonnumericBeginning && i == 0) {
        uuid[i] = alphabet[0 | (Math.random() * alphabet.length)]
      } else {
        uuid[i] = chars[0 | (Math.random() * radix)]
      }

    }
  } else {
    // rfc4122, version 4 form
    let r

    // rfc4122 requires these characters
    uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-'
    uuid[14] = '4'

    // Fill in random data.  At i==19 set the high bits of clock sequence as
    // per rfc4122, sec. 4.1.5
    for (i = 0; i < 36; i++) {
      if (!uuid[i]) {
        r = 0 | (Math.random() * 16)
        uuid[i] = chars[i == 19 ? (r & 0x3) | 0x8 : r]
      }
    }
  }

  return uuid.join('')
}

function randomRgb() {
  return `rgb(
${Math.floor(Math.random() * 256)}
,${Math.floor(Math.random() * 256)}
,${Math.floor(Math.random() * 256)}
)`
}

function randomRgba(diaphaneity: number = 1) {
  return `rgba(
${Math.floor(Math.random() * 256)}
,${Math.floor(Math.random() * 256)}
,${Math.floor(Math.random() * 256)},
${diaphaneity}
)`
}

function getOS() {
  if (navigator.userAgent.indexOf('Window') > 0) {
    return 'Windows'
  } else if (navigator.userAgent.indexOf('Mac OS X') > 0) {
    return 'Mac'
  } else if (navigator.userAgent.indexOf('Linux') > 0) {
    return 'Linux'
  } else {
    return 'NUll'
  }
}

/**
 * 扁平化对象
 */
function flatten(data: Object): Record<string, any> {
  const result: Record<string, any> = {};
  const isEmpty = (x: Record<string, any>) => Object.keys(x).length === 0;
  const recurse = (cur: Record<string, any>, prop: string) => {
    if (Object(cur) !== cur) {
      result[prop] = cur;
    } else if (Array.isArray(cur)) {
      const length = cur.length;
      for (let i = 0; i < length; i++) {
        recurse(cur[i], `${prop}[${i}]`);
      }
      if (length === 0) {
        result[prop] = [];
      }
    } else {
      if (!isEmpty(cur)) {
        Object.keys(cur).forEach((key) =>
          recurse(cur[key], prop ? `${prop}.${key}` : key)
        );
      } else {
        result[prop] = {};
      }
    }
  };
  recurse(data, "");
  return result;
};


const ScriptHelper = {
  execute(scriptStr: string, options: unknown): unknown {
    return Function('"use strict";return (' + scriptStr + ')')()(options);
  },
  executeEl(callObject: unknown, logicStr: string): unknown {
    return Function('"use strict";return (function(){ return ' + logicStr + '})')().call(
      callObject,
    );
  },
};

export { uuid, randomRgb, randomRgba, getOS, flatten, ScriptHelper }
