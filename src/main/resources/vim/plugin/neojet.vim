
fun! s:OnTextChanged(...)
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
            \ 'type': 'inc',
            \ 'mod': &modified,
            \ 'start': line('.') - 1,
            \ 'end': line('.') - 1,
            \ 'text': getline('.'),
            \ })
    else
        let l:flag = a:0 ? a:1 : ''

        call neojet#bvent('text_changed', {
            \ 'type': 'range',
            \ 'mod': &modified,
            \ 'start': line('w0') - 1,
            \ 'end': line('w$') - 1,
            \ 'text': l:flag,
            \ })
    endif
endfun

augroup neojet_autocmds
    autocmd!
    autocmd BufWinEnter,BufReadPost * call neojet#rpc("buf_win_enter", expand('%:p'))
    autocmd BufWritePost * call <SID>OnTextChanged('BufWritePost')
    autocmd InsertEnter,InsertLeave * call <SID>OnTextChanged()
    autocmd TextChanged,TextChangedI * call <SID>OnTextChanged()
    autocmd CursorMoved,CursorMovedI * call <SID>OnTextChanged()
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

if !get(g:, '_neojet_init')
    " add the parent of the containing directory to the rtp
    "  so our autocommands can get sourced
    exe 'set rtp+=' . substitute(resolve(expand('<sfile>:p:h:h')), ' ', '\\ ', '')
endif

let g:_neojet_init = 1
