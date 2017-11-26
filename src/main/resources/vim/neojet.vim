
let s:loclist_var= '_neojet_loclist'

let s:rpcnotify = function('rpcnotify')

fun! neojet#rpc(method, ...)
    let l:args = [1, a:method] + a:000
    call call(s:rpcnotify, l:args)
endfun
let s:_rpc = function('neojet#rpc')

fun! neojet#context()
    " NOTE: the ordering here MUST match that of the
    "  properties of BufferEventArg
    let [l:_, l:lnum, l:col, l:_, l:_] = getcurpos()
    let l:offset = line2byte(l:lnum) + l:col - 2

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

" FIXME move these to an appropriate autoload directory,
" and come up with a clever way to externalize them from
" the JAR at runtime, and for Vim to then load them
" (for the latter we can probably just add the right path
" to the runtime)

fun! neojet#hl_update(bufnr, list)
    call setbufvar(a:bufnr, s:loclist_var, a:list)

    if exists('g:ale_enabled')
        " integrate with ALE
        if !exists('g:ale_buffer_info')
            let g:ale_buffer_info = {}
        endif
        if ale#engine#InitBufferInfo(a:bufnr)
            let b:ale_linters = {'java': ['neojet']}
        endif

        let g:ale_buffer_info[a:bufnr].loclist = a:list
        call ale#engine#SetResults(a:bufnr, a:list)
        return
    endif

    " TODO if no ALE, do it ourselves?
    call setloclist(bufwinnr(a:bufnr), a:list, 'r')
endfun

fun! neojet#hl_create(bufnr, id, start, end, desc, severity)
    let l:winnr = bufwinnr(a:bufnr)

    let [l:lnum, l:col] = neojet#offset2lc(a:start)
    let [l:end_lnum, l:end_col] = neojet#offset2lc(a:end)

    " the end_col is the offset *after* the end, so -1
    let l:list = getbufvar(a:bufnr, s:loclist_var, []) +
               \ [{'bufnr': a:bufnr,
                 \ 'nr': a:id,
                 \ 'lnum': l:lnum,
                 \ 'col': l:col,
                 \ 'end_lnum': l:end_lnum,
                 \ 'end_col': l:end_col - 1,
                 \ 'text': a:desc,
                 \ 'type': a:severity[0],
                 \ 'linter_name': 'neojet',
                 \ }]
    call neojet#hl_update(a:bufnr, l:list)
endfun

fun! neojet#hl_delete(bufnr, id, start, end, desc, severity)
    let l:list = getbufvar(a:bufnr, s:loclist_var, [])
    call filter(l:list, 'v:val.nr != ' . a:id)
    call neojet#hl_update(a:bufnr, l:list)
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
            \ 'start': line('.') - 1,
            \ 'end': line('.') - 1,
            \ 'text': getline('.'),
            \ })
    else
        call neojet#bvent('text_changed', {
            \ 'type': 'range',
            \ 'mod': &modified,
            \ 'start': line('w0') - 1,
            \ 'end': line('w$') - 1,
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


" prepare ALE compat layer, if necessary
fun! neojet#_AleCallback(...)
    " ignore everything
endfun

if exists('g:ale_enabled') && !get(g:, '_neojet_init')
    call ale#linter#Define('java', {
        \ 'name': 'neojet',
        \ 'callback': 'neojet#_AleCallback',
        \ 'command_callback': 'neojet#_AleCallback',
        \ 'executable_callback': 'neojet#_AleCallback',
        \ })
endif

let g:_neojet_init = 1
