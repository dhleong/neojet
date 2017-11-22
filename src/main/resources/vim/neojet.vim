
let s:rpcnotify = function('rpcnotify')

fun! neojet#rpc(method, ...)
    let l:args = [1, a:method] + a:000
    call call(s:rpcnotify, l:args)
endfun
let s:_rpc = function('neojet#rpc')

fun! neojet#bvent(event, ...)
    let l:args = [a:event, bufnr('%')] + a:000
    call call(s:_rpc, l:args)
endfun

fun! s:OnTextChanged()
    call neojet#bvent('text_changed')
endfun

augroup neojet_autocmds
    autocmd!
    autocmd BufWinEnter,BufReadPost * call neojet#rpc("buf_win_enter", expand('%:p'))
    autocmd TextChanged,TextChangedI * call s:OnTextChanged()
augroup END

let g:neojet#version = 1
