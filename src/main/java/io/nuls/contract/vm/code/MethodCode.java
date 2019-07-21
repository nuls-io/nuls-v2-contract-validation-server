/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.contract.vm.code;

import com.google.common.base.Joiner;
import io.nuls.contract.validation.service.impl.*;
import io.nuls.contract.validation.util.CommonUtil;
import io.nuls.contract.vm.program.ProgramMethodArg;
import io.nuls.contract.vm.program.impl.ProgramDescriptors;
import io.nuls.contract.vm.util.Constants;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.ListUtils;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.contract.vm.util.Utils.arrayListInitialCapacity;

@ToString
@EqualsAndHashCode
public class MethodCode {

    public static final String VIEW_ANNOTATION_DESC = "Lio/nuls/contract/sdk/annotation/View;";
    public static final String PAYABLE_ANNOTATION_DESC = "Lio/nuls/contract/sdk/annotation/Payable;";
    public static final String REQUIRED_ANNOTATION_DESC = "Lio/nuls/contract/sdk/annotation/Required;";

    /**
     * The method's access flags (see {@link Opcodes}). This field also indicates if the method is
     * synthetic and/or deprecated.
     */
    public final int access;

    /**
     * The method's name.
     */
    public final String name;

    /**
     * The method's descriptor (see {@link Type}).
     */
    public final String desc;

    /**
     * The method's signature. May be <tt>null</tt>.
     */
    public final String signature;

    /**
     * The internal names of the method's exception classes (see {@link Type#getInternalName()}).
     */
    public final List<String> exceptions;

    /**
     * The method parameter info (access flags and name)
     */
    public final List<ParameterNode> parameters;


    /**
     * The runtime visible annotations of this method. May be <tt>null</tt>.
     */
    public final List<AnnotationNode> visibleAnnotations;

    /**
     * The runtime invisible annotations of this method. May be <tt>null</tt>.
     */
    public final List<AnnotationNode> invisibleAnnotations;

    /**
     * The runtime visible type annotations of this method. May be <tt>null</tt>.
     */
    public final List<TypeAnnotationNode> visibleTypeAnnotations;

    /**
     * The runtime invisible type annotations of this method. May be <tt>null</tt>.
     */
    public final List<TypeAnnotationNode> invisibleTypeAnnotations;

    /**
     * The non standard attributes of this method. May be <tt>null</tt>.
     */
    public final List<Attribute> attrs;

    /**
     * The default value of this annotation interface method. This field must be a {@link Byte},
     * {@link Boolean}, {@link Character}, {@link Short}, {@link Integer}, {@link Long}, {@link
     * Float}, {@link Double}, {@link String} or {@link Type}, or an two elements String array (for
     * enumeration values), a {@link AnnotationNode}, or a {@link List} of values of one of the
     * preceding types. May be <tt>null</tt>.
     */
    public final Object annotationDefault;

    /**
     * The number of method parameters than can have runtime visible annotations. This number must be
     * less or equal than the number of parameter types in the method descriptor (the default value 0
     * indicates that all the parameters described in the method descriptor can have annotations). It
     * can be strictly less when a method has synthetic parameters and when these parameters are
     * ignored when computing parameter indices for the purpose of parameter annotations (see
     * https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.18).
     */
    public final int visibleAnnotableParameterCount;

    /**
     * The runtime visible parameter annotations of this method. These lists are lists of {@link
     * AnnotationNode} objects. May be <tt>null</tt>.
     */
    public final List<AnnotationNode>[] visibleParameterAnnotations;

    /**
     * The number of method parameters than can have runtime invisible annotations. This number must
     * be less or equal than the number of parameter types in the method descriptor (the default value
     * 0 indicates that all the parameters described in the method descriptor can have annotations).
     * It can be strictly less when a method has synthetic parameters and when these parameters are
     * ignored when computing parameter indices for the purpose of parameter annotations (see
     * https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.18).
     */
    public final int invisibleAnnotableParameterCount;

    /**
     * The runtime invisible parameter annotations of this method. These lists are lists of {@link
     * AnnotationNode} objects. May be <tt>null</tt>.
     */
    public final List<AnnotationNode>[] invisibleParameterAnnotations;

    /**
     * The instructions of this method.
     */
    public final InsnList instructions;

    /**
     * The try catch blocks of this method.
     */
    public final List<TryCatchBlockNode> tryCatchBlocks;

    /**
     * The maximum stack size of this method.
     */
    public final int maxStack;

    /**
     * The maximum number of local variables of this method.
     */
    public final int maxLocals;

    /**
     * The local variables of this method. May be <tt>null</tt>
     */
    //public final List<LocalVariableNode> localVariables;
    public final List<LocalVariableCode> localVariables;

    /**
     * The visible local variable annotations of this method. May be <tt>null</tt>
     */
    public final List<LocalVariableAnnotationNode> visibleLocalVariableAnnotations;

    /**
     * The invisible local variable annotations of this method. May be <tt>null</tt>
     */
    public final List<LocalVariableAnnotationNode> invisibleLocalVariableAnnotations;

    /**
     * Whether the accept method has been called on this object.
     */
    //private final boolean visited;

    public final ClassCode classCode;

    public final String className;

    public final String nameDesc;

    public final String fullName;

    public final boolean isPublic;

    public final boolean isStatic;

    public final boolean isAbstract;

    public final boolean isNative;

    public final boolean isClinit;

    public final boolean isConstructor;

    public final VariableType returnVariableType;

    public final List<VariableType> argsVariableType;

    //contract

    public final String returnArg;

    public final List<ProgramMethodArg> args;

    public final String normalDesc;

    public MethodCode(ClassCode classCode, MethodNode methodNode) {
        access = methodNode.access;
        name = methodNode.name;
        desc = methodNode.desc;
        signature = methodNode.signature;
        exceptions = ListUtils.emptyIfNull(methodNode.exceptions);
        parameters = ListUtils.emptyIfNull(methodNode.parameters);
        visibleAnnotations = ListUtils.emptyIfNull(methodNode.visibleAnnotations);
        invisibleAnnotations = ListUtils.emptyIfNull(methodNode.invisibleAnnotations);
        visibleTypeAnnotations = ListUtils.emptyIfNull(methodNode.visibleTypeAnnotations);
        invisibleTypeAnnotations = ListUtils.emptyIfNull(methodNode.invisibleTypeAnnotations);
        attrs = ListUtils.emptyIfNull(methodNode.attrs);
        annotationDefault = methodNode.annotationDefault;
        visibleAnnotableParameterCount = methodNode.visibleAnnotableParameterCount;
        visibleParameterAnnotations = methodNode.visibleParameterAnnotations;
        invisibleAnnotableParameterCount = methodNode.invisibleAnnotableParameterCount;
        invisibleParameterAnnotations = methodNode.invisibleParameterAnnotations;
        instructions = methodNode.instructions;
        tryCatchBlocks = ListUtils.emptyIfNull(methodNode.tryCatchBlocks);
        maxStack = methodNode.maxStack;
        maxLocals = methodNode.maxLocals;
        //localVariables = ListUtils.emptyIfNull(methodNode.localVariables);
        visibleLocalVariableAnnotations = ListUtils.emptyIfNull(methodNode.visibleLocalVariableAnnotations);
        invisibleLocalVariableAnnotations = ListUtils.emptyIfNull(methodNode.invisibleLocalVariableAnnotations);
        //
        this.classCode = classCode;
        className = classCode.name;
        nameDesc = name + desc;
        fullName = className + "." + nameDesc;
        isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
        isStatic = (access & Opcodes.ACC_STATIC) != 0;
        isAbstract = (access & Opcodes.ACC_ABSTRACT) != 0;
        isNative = (access & Opcodes.ACC_NATIVE) != 0;
        isClinit = Constants.CLINIT_NAME.equals(name);
        isConstructor = Constants.CONSTRUCTOR_NAME.equals(name);
        //
        final List<VariableType> variableTypes = VariableType.parseAll(desc);
        final int last = variableTypes.size() - 1;
        returnVariableType = variableTypes.get(last);

        argsVariableType = variableTypes.subList(0, last);
        final List<LocalVariableNode> localVariableNodes = ListUtils.emptyIfNull(methodNode.localVariables);
        localVariables = new ArrayList<>(arrayListInitialCapacity(localVariableNodes.size()));

        for (LocalVariableNode localVariableNode : localVariableNodes) {
            // skip localVariable'name contains '$'
            if(localVariableNode.name != null && localVariableNode.name.contains("$")) {
                continue;
            }
            localVariables.add(new LocalVariableCode(localVariableNode));
        }

        //contract
        returnArg = ProgramDescriptors.getNormalDesc(returnVariableType);
        args = new ArrayList<>(arrayListInitialCapacity(argsVariableType.size()));
        final List<String> stringArgs = new ArrayList<>(arrayListInitialCapacity(argsVariableType.size()));
        int index = 0;
        if (!isStatic) {
            index += 1;
        }
        int start = 0;
        for (int i = 0; i < argsVariableType.size(); i++) {
            final VariableType variableType = argsVariableType.get(i);
            if (i > 0) {
                final VariableType previousVariableType = argsVariableType.get(i - 1);
                if (previousVariableType.isLong() || previousVariableType.isDouble()) {
                    index += 1;
                }
            }
            String name = "var" + (i + 1);

            LocalVariableCode localVariableCode = getLocalVariableCode(index);
            index++;

            // 成员内部类的构造函数中过滤掉`this`参数
            if(isConstructor) {
                do {
                    List<InnerClassNode> innerClasses = classCode.innerClasses;
                    if(localVariables == null || localVariables.isEmpty()) break;
                    if(variableTypes == null || variableTypes.isEmpty()) break;
                    if(innerClasses == null || innerClasses.isEmpty()) break;
                    InnerClassNode innerClassNode = innerClasses.get(0);
                    String innerName = innerClassNode.innerName;
                    String outerName = innerClassNode.outerName;
                    String thisInnerName = classCode.name.substring(classCode.name.lastIndexOf("$") + 1);
                    String firstVariableType = variableTypes.get(0).getType();
                    String firstLocalVariableName = this.localVariables.get(0).name;
                    if(thisInnerName.equals(innerName)
                            && firstVariableType.equals(outerName)
                            && "this".equals(firstLocalVariableName)) {
                        start = 1;
                    }
                }while (false);
            }

            if (localVariableCode != null) {
                name = localVariableCode.name;
                if (isConstructor && classCode.isSyntheticField(name)) {
                    continue;
                }
//                if (!variableType.equals(localVariableCode.variableType)) {
//                    System.out.println();
//                }
            }

            if(start == 1) {
                continue;
            }

            final String normalDesc = ProgramDescriptors.getNormalDesc(variableType);
            final String stringArg = normalDesc + " " + name;
            stringArgs.add(stringArg);
            final ProgramMethodArg arg = new ProgramMethodArg(normalDesc, name, hasRequiredAnnotation(i));
            args.add(arg);
        }

        // 成员内部类的构造函数 localVariables 过滤掉`this`参数
        if(start == 1) {
            localVariables.remove(0);
        }

        // 成员内部类的构造函数 localVariables 过滤掉`this$0`参数
        if(localVariables != null && !localVariables.isEmpty()) {
            LocalVariableCode localVariableCode = localVariables.get(0);
            if (isConstructor && classCode.isSyntheticField(localVariableCode.name)) {
                localVariables.remove(0);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(Joiner.on(", ").join(stringArgs));
        sb.append(") return ");
        sb.append(returnArg);
        normalDesc = sb.toString();

//        String desc = ProgramDescriptors.parseDesc(normalDesc);
//        if (!desc.equals(desc)) {
//            System.out.println();
//        }
    }

    public boolean hasViewAnnotation() {
        return hasAnnotation(VIEW_ANNOTATION_DESC);
    }

    public boolean hasPayableAnnotation() {
        return hasAnnotation(PAYABLE_ANNOTATION_DESC);
    }

    public boolean hasAnnotation(String annotation) {
        return visibleAnnotations.stream()
                .anyMatch(annotationNode -> annotation.equals(annotationNode.desc));
    }

    private boolean hasRequiredAnnotation(int i) {
        if (!(visibleParameterAnnotations != null && visibleParameterAnnotations.length > 0 && visibleParameterAnnotations.length > i)) {
            return false;
        }
        List<AnnotationNode> list = visibleParameterAnnotations[i];
        if (list == null) {
            return false;
        }
        return list.stream().anyMatch(annotationNode -> REQUIRED_ANNOTATION_DESC.equals(annotationNode.desc));
    }

    public LocalVariableCode getLocalVariableCode(int index) {
        return localVariables.stream().filter(localVariableCode -> localVariableCode.index == index)
                .findFirst().orElse(null);
    }

    public boolean isClass(String className) {
        return this.className.equals(className);
    }

    public boolean isMethod(String name, String desc) {
        return this.name.equals(name) && this.desc.equals(desc);
    }

    public boolean isMethod(String className, String name, String desc) {
        return this.className.equals(className) && this.name.equals(name) && this.desc.equals(desc);
    }

    public boolean customizeEquals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodCode)) return false;

        MethodCode that = (MethodCode) o;

        if(hasViewAnnotation() != that.hasViewAnnotation()) return false;
        if(hasPayableAnnotation() != that.hasPayableAnnotation()) return false;
        if (access != that.access) return false;
        if (visibleAnnotableParameterCount != that.visibleAnnotableParameterCount) return false;
        if (invisibleAnnotableParameterCount != that.invisibleAnnotableParameterCount) return false;
        if (maxStack != that.maxStack) return false;
        if (maxLocals != that.maxLocals) return false;
        if (isPublic != that.isPublic) return false;
        if (isStatic != that.isStatic) return false;
        if (isAbstract != that.isAbstract) return false;
        if (isNative != that.isNative) return false;
        if (isClinit != that.isClinit) return false;
        if (isConstructor != that.isConstructor) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (desc != null ? !desc.equals(that.desc) : that.desc != null) return false;
        if (!ListUtils.isEqualList(exceptions, that.exceptions)) return false;
        if (!CommonUtil.isEqualList(parameters, that.parameters, ParameterNodeEqual.getInstance())) return false;
        if (!CommonUtil.isEqualList(visibleAnnotations, that.visibleAnnotations, AnnotationNodeEqual.getInstance())) return false;
        if (!CommonUtil.isEqualList(visibleTypeAnnotations, that.visibleTypeAnnotations, TypeAnnotationNodeEqual.getInstance())) return false;
        if (!CommonUtil.isEqualList(invisibleAnnotations, that.invisibleAnnotations, AnnotationNodeEqual.getInstance())) return false;
        if (!CommonUtil.isEqualList(invisibleTypeAnnotations, that.invisibleTypeAnnotations, TypeAnnotationNodeEqual.getInstance())) return false;
        if (!CommonUtil.isEqualList(attrs, that.attrs, AttributeEqual.getInstance())) return false;
        if (!CommonUtil.equalsCollectionArray(visibleParameterAnnotations, that.visibleParameterAnnotations, AnnotationNodeEqual.getInstance())) return false;
        if (!CommonUtil.equalsCollectionArray(invisibleParameterAnnotations, that.invisibleParameterAnnotations, AnnotationNodeEqual.getInstance())) return false;
        if (!CommonUtil.isEqualList(localVariables, that.localVariables, LocalVariableCodeEqual.getInstance())) return false;
        if (!CommonUtil.isEqualList(visibleLocalVariableAnnotations, that.visibleLocalVariableAnnotations, LocalVariableAnnotationNodeEqual.getInstance())) return false;
        if (!CommonUtil.isEqualList(invisibleLocalVariableAnnotations, that.invisibleLocalVariableAnnotations, LocalVariableAnnotationNodeEqual.getInstance())) return false;
        if (className != null ? !className.equals(that.className) : that.className != null) return false;
        if (nameDesc != null ? !nameDesc.equals(that.nameDesc) : that.nameDesc != null) return false;
        if (fullName != null ? !fullName.equals(that.fullName) : that.fullName != null) return false;
        if (returnArg != null ? !returnArg.equals(that.returnArg) : that.returnArg != null) return false;
        if (!ListUtils.isEqualList(args, that.args)) return false;
        if (normalDesc != null ? !normalDesc.equals(that.normalDesc) : that.normalDesc != null) return false;

        return true;
    }

}
