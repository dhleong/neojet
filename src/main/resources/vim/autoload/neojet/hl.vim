
let s:loclist_var= '_neojet_loclist'


fun! neojet#hl#SetList(bufnr, list)
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

fun! neojet#hl#create(bufnr, id, start, end, desc, severity)
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
    call neojet#hl#SetList(a:bufnr, l:list)
endfun

fun! neojet#hl#delete(bufnr, id, start, end, desc, severity)
    let l:list = getbufvar(a:bufnr, s:loclist_var, [])
    call filter(l:list, 'v:val.nr != ' . a:id)
    call neojet#hl#SetList(a:bufnr, l:list)
endfun

