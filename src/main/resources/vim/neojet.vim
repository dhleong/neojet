
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

fun! neojet#offset2lc(offset)
    let l:lnum = byte2line(a:offset)
    let l:lineStartOffset = line2byte(l:lnum)
    let l:col = a:offset - l:lineStartOffset
    return [l:lnum, l:col]
endfun


fun! neojet#hl_create(bufnr, id, start, end, desc, severity)
    let l:winnr = bufwinnr(a:bufnr)

    let [l:lnum, l:col] = neojet#offset2lc(a:start)

    let l:list = [{'bufnr': a:bufnr,
                 \ 'nr': a:id,
                 \ 'lnum': l:lnum,
                 \ 'col': l:col,
                 \ 'text': a:desc,
                 \ 'type': a:severity[0]
                 \ }]
    call setloclist(l:winnr, l:list, 'a')
endfun

fun! neojet#hl_delete(bufnr, id, start, end, desc, severity)
    let l:winnr = bufwinnr(a:bufnr)
    let l:list = getloclist(l:winnr)
    call filter(l:list, 'v:val.nr != ' . a:id)
    call setloclist(l:winnr, l:list, 'r')
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
            \ 'mod': &modified,
            \ 'start': line('.'),
            \ 'end': line('.'),
            \ 'text': getline('.'),
            \ })
    else
        call neojet#bvent('text_changed', {
            \ 'type': 'range',
            \ 'mod': &modified,
            \ 'start': line('w0'),
            \ 'end': line('w$'),
            \ 'text': '',
            \ })
    endif
endfun

augroup neojet_autocmds
    autocmd!
    autocmd BufWinEnter,BufReadPost * call neojet#rpc("buf_win_enter", expand('%:p'))
    autocmd BufWritePost * call s:OnTextChanged()
    autocmd TextChanged,TextChangedI * call s:OnTextChanged()
    autocmd CursorMoved,CursorMovedI * call s:OnTextChanged()
augroup END

let g:neojet#version = 1
