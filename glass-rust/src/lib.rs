#[link(wasm_import_module = "kotlin_module")]
extern "C" {
    fn method1();
    fn method2(str: *const u8, str_length: usize);
}

#[no_mangle]
pub unsafe extern "C" fn run() {
    method1(); // Call the Kotlin function

    const STR: &str = "Hello, World";
    method2(STR.as_ptr(), STR.len());
}