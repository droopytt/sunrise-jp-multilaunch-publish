rmdir runtime -Recurse -Force
jlink --add-modules java.base,java.desktop,java.naming,java.net.http --output runtime
