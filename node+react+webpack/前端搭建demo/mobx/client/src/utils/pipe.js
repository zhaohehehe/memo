/*
 * utils for pipe
 * 
 */
/**
 * 值转化通道
 * @param arr 要转化的数组
 * @param value 值
 * @param valueKey 值Key
 * @param displayKey 返回值Key
 * @param loopKey 递归的key, 如果不存在就不进行递归
 * @return display or value
 */
export const value2DisplayPipe = (arr, value, valueKey = 'value', displayKey = 'text', loopKey) => {
    let result = value;
    if (loopKey) {
        const loop = (arr) => {
            for (let i = 0, l = arr.length; i < l; i++) {
                if (arr[i][valueKey] === value) {
                    result = arr[i][displayKey];
                } else {
                    arr[i][loopKey] && loop(arr[i][loopKey]);
                }
            }
        };
        loop(arr);
        return result;
    }

    let temp = arr.find(item => item[valueKey] === value);
    if (temp) {
        result = temp[displayKey];
    }
    return result;
};

