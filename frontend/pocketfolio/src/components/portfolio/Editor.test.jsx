import React, {Component} from 'react';
import {CKEditor} from '@ckeditor/ckeditor5-react';
import ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import Nav from '../common/nav';
const Editor = () => {
  return (

    <CKEditor
      editor={ClassicEditor}
      data=""
      config={{
        placeholder: '내용을 입력하세요.',
      }}
      onReady={editor => {
        // You can store the "editor" and use when it is needed.
        console.log('Editor is ready to use!', editor);
      }}
      onChange={(event, editor) => {
        const data = editor.getData();
        console.log({event, editor, data});
      }}
      onBlur={(event, editor) => {
        console.log('Blur.', editor);
      }}
      onFocus={(event, editor) => {
        console.log('Focus.', editor);
      }}
    />
    
    
  );
};

export default Editor;
