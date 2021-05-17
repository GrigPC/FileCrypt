# FileCrypt

FileCrypt is a simple file encryption program.
The program uses AES-128 encryption, one of the most reliable solution (to date) for encrypting information.

Instructions:
- Run the .bat file to open the program.
- Select up to 10 files that you would like to encrypt or decrypt at once.
- Select a destination folder, where your encrypted/decrypted files will be saved
- Insert the key/password you would like to use.
- Click 'Encrypt' or 'Decrypt'.


As the program operates on the byte composition of files (indistinctily of format for now), it will theoretically be able to encrypt and decrypt any file that is not dependent on 'headers' to run/view.
The program uses Java's built-in input/output stream methods, all of which utilize Big Endian, hence there is not much concern for erronous
