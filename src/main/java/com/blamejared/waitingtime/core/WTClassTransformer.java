package com.blamejared.waitingtime.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class WTClassTransformer implements IClassTransformer {
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(name.equals("net.minecraftforge.fml.client.SplashProgress")) {
            System.out.println("Found SplashProgress");
            ClassNode classNode = readClassFromBytes(basicClass);
            MethodNode targetMethod = null;
            //Finds the target method
            for(MethodNode methodNode : classNode.methods) {
                if(methodNode.name.equals("start")) {
                    targetMethod = methodNode;
                    break;
                }
            }
            //Finds the target method, should be version agnostic
            AbstractInsnNode targetNode = null;
            for(AbstractInsnNode insnNode : targetMethod.instructions.toArray()) {
                if(insnNode.getOpcode() == Opcodes.INVOKESPECIAL) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                    if(methodInsnNode.owner.equals("net/minecraftforge/fml/client/SplashProgress$3") && methodInsnNode.name.equals("<init>")) {
                        targetNode = insnNode;
                        break;
                    }
                }
            }
            
            InsnList insnList = new InsnList();
            
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/blamejared/waitingtime/CustomThread", "createNewThread", "()Ljava/lang/Thread;", false));
            insnList.add(new FieldInsnNode(Opcodes.PUTSTATIC, "net/minecraftforge/fml/client/SplashProgress", "thread", "Ljava/lang/Thread;"));
            
            
            targetMethod.instructions.insert(targetNode, insnList);
            
            System.out.println("Patched " + name);
            
            return writeClassToBytes(classNode);
            
        }
        return basicClass;
    }
    
    
    private ClassNode readClassFromBytes(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        return classNode;
    }
    
    private byte[] writeClassToBytes(ClassNode classNode) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
    
}