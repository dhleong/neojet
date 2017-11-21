
let s:rpcnotify = function('rpcnotify')

function! s:rpc(method, ...)
    let args = [1, a:method] + a:000
    call call(s:rpcnotify, args)
endfunction

function! NeoRpc(method, ...)
    let args = [1, a:method] + a:000
    call call(s:rpcnotify, args)
endfunction


augroup neojet_autocmds
    autocmd!
    autocmd BufWinEnter * call s:rpc("buf_win_enter", expand('%:p'))
augroup END

let g:neojet#version = 1
