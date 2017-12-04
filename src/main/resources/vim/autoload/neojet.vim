
let s:rpcnotify = function('rpcnotify')

fun! neojet#rpc(method, ...)
    let l:args = [1, a:method] + a:000
    call call(s:rpcnotify, l:args)
endfun
let s:_rpc = function('neojet#rpc')

fun! neojet#context()
    let [l:_, l:lnum, l:col, l:_, l:_] = getcurpos()
    let l:offset = line2byte(l:lnum) + l:col - 2

    " NOTE: the ordering here MUST match that of the
    "  properties of BufferEventArg
    return [
        \ bufnr('%'),
        \ l:offset,
        \ ]
endfun

fun! neojet#bvent(event, ...)
    let l:args = [a:event] + neojet#context() + a:000
    call call(s:_rpc, l:args)
endfun

fun! neojet#offset2lc(offset)
    " IntelliJ offsets are 0-based, but vim's are 1-based;
    "  byte2line(0) -> -1
    let l:offset = a:offset + 1
    let l:lnum = byte2line(l:offset)
    let l:lineStartOffset = line2byte(l:lnum)
    let l:col = l:offset - l:lineStartOffset

    " vim's cols are also 1-based, but this math is 0-based
    return [l:lnum, l:col + 1]
endfun

