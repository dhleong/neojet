
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
    if !exists('b:last_change_tick')
        let b:last_change_tick = -1
    endif

    if b:changedtick <= b:last_change_tick
        " nothing actually changed yet
        return
    endif

    let b:last_change_tick = b:changedtick

    if mode() ==# 'i'
        call neojet#bvent('text_changed', {
            \ 'type': 'incremental',
            \ 'start': line('.'),
            \ 'end': line('.'),
            \ 'text': getline('.'),
            \ })
    else
        call neojet#bvent('text_changed', {
            \ 'type': 'range',
            \ 'start': line('w0'),
            \ 'end': line('w$'),
            \ 'text': '',
            \ })
    endif
endfun

augroup neojet_autocmds
    autocmd!
    autocmd BufWinEnter,BufReadPost * call neojet#rpc("buf_win_enter", expand('%:p'))
    autocmd TextChanged,TextChangedI * call s:OnTextChanged()
    autocmd CursorMoved,CursorMovedI * call s:OnTextChanged()
augroup END

let g:neojet#version = 1
