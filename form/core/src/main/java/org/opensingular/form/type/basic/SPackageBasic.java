/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensingular.form.type.basic;

import org.opensingular.form.AtrRef;
import org.opensingular.form.PackageBuilder;
import org.opensingular.form.SDictionary;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SIConsumer;
import org.opensingular.form.SIList;
import org.opensingular.form.SIPredicate;
import org.opensingular.form.SISupplier;
import org.opensingular.form.SInfoPackage;
import org.opensingular.form.SInstance;
import org.opensingular.form.SPackage;
import org.opensingular.form.SType;
import org.opensingular.form.STypeBehavior;
import org.opensingular.form.STypeConsumer;
import org.opensingular.form.STypeList;
import org.opensingular.form.STypePredicate;
import org.opensingular.form.STypeSimple;
import org.opensingular.form.STypeSupplier;
import org.opensingular.form.enums.PhraseBreak;
import org.opensingular.form.type.core.SIBoolean;
import org.opensingular.form.type.core.SIDate;
import org.opensingular.form.type.core.SIInteger;
import org.opensingular.form.type.core.SILong;
import org.opensingular.form.type.core.SIString;
import org.opensingular.form.type.core.STypeBoolean;
import org.opensingular.form.type.core.STypeDate;
import org.opensingular.form.type.core.STypeDecimal;
import org.opensingular.form.type.core.STypeFormula;
import org.opensingular.form.type.core.STypeInteger;
import org.opensingular.form.type.core.STypeLong;
import org.opensingular.form.type.core.STypeString;
import org.opensingular.form.type.core.annotation.STypeAnnotationClassifierList;
import org.opensingular.form.type.core.attachment.STypeAttachment;
import org.opensingular.lib.commons.lambda.IConsumer;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings({ "unchecked", "rawtypes" })
@SInfoPackage(name = SPackageBasic.NAME)
public class SPackageBasic extends SPackage {

    public static final String NAME = SDictionary.SINGULAR_PACKAGES_PREFIX + "basic";

    //@formatter:off
    public static final AtrRef<?, ?, Object>                                      ATR_DEFAULT_IF_NULL       = AtrRef.ofSelfReference(SPackageBasic.class, "defaultIfNull");
    public static final AtrRef<?, ?, Object>                                      ATR_INITIAL_VALUE         = AtrRef.ofSelfReference(SPackageBasic.class, "initialValue" );
    public static final AtrRef<STypeString, SIString, String>                     ATR_LABEL                 = new AtrRef<>(SPackageBasic.class, "label"                 , STypeString.class, SIString.class, String.class);
    public static final AtrRef<STypeBoolean, SIBoolean, Boolean>                  ATR_ENABLE_HTML_IN_LABEL  = new AtrRef<>(SPackageBasic.class, "enableHTMLInLabel"            , STypeBoolean.class, SIBoolean.class, Boolean.class);
    public static final AtrRef<STypeBoolean, SIBoolean, Boolean>                  ATR_TRIM                  = new AtrRef<>(SPackageBasic.class, "trim"                  , STypeBoolean.class, SIBoolean.class, Boolean.class);
    public static final AtrRef<STypeFormula, SIComposite, Object>                 ATR_FORMULA               = new AtrRef<>(SPackageBasic.class, "formula"               , STypeFormula.class, SIComposite.class, Object.class);
    public static final AtrRef<STypeBoolean, SIBoolean, Boolean>                  ATR_EMPTY_TO_NULL         = new AtrRef<>(SPackageBasic.class, "emptyToNull"           , STypeBoolean.class, SIBoolean.class, Boolean.class);
    public static final AtrRef<STypeString, SIString, String>                     ATR_SUBTITLE              = new AtrRef<>(SPackageBasic.class, "subtitle"              , STypeString.class, SIString.class, String.class);
    public static final AtrRef<STypeString, SIString, String>                     ATR_BASIC_MASK            = new AtrRef<>(SPackageBasic.class, "basicMask"             , STypeString.class, SIString.class, String.class);
    public static final AtrRef<STypeString, SIString, String>                     ATR_REGEX_MASK            = new AtrRef<>(SPackageBasic.class, "regexMask"             , STypeString.class, SIString.class, String.class);
    public static final AtrRef<STypeInteger, SIInteger, Integer>                  ATR_MAX_LENGTH            = new AtrRef<>(SPackageBasic.class, "maxLength"             , STypeInteger.class, SIInteger.class, Integer.class);
    public static final AtrRef<STypeInteger, SIInteger, Integer>                  ATR_INTEGER_MAX_LENGTH    = new AtrRef<>(SPackageBasic.class, "integerMaxLength"      , STypeInteger.class, SIInteger.class, Integer.class);
    public static final AtrRef<STypeInteger, SIInteger, Integer>                  ATR_FRACTIONAL_MAX_LENGTH = new AtrRef<>(SPackageBasic.class, "fractionalMaxLength"   , STypeInteger.class, SIInteger.class, Integer.class);
    public static final AtrRef<STypeString, SIString, String>                     ATR_DISPLAY_STRING        = new AtrRef<>(SPackageBasic.class, "displayString"         , STypeString.class, SIString.class, String.class);
    public static final AtrRef<STypeInteger, SIInteger, Integer>                  ATR_DISPLAY_ORDER         = new AtrRef<>(SPackageBasic.class, "displayOrder"          , STypeInteger.class, SIInteger.class, Integer.class);
    public static final AtrRef<STypeString, SIString, String>                     ATR_HELP                  = new AtrRef<>(SPackageBasic.class, "help"                  , STypeString.class, SIString.class, String.class);
    public static final AtrRef<STypeString, SIString, String>                     ATR_INSTRUCTION           = new AtrRef<>(SPackageBasic.class, "instruction"           , STypeString.class, SIString.class, String.class);
    public static final AtrRef<STypeAnnotationClassifierList, SIList, List>       ATR_ANNOTATED             = new AtrRef<>(SPackageBasic.class, "anotated"              , STypeAnnotationClassifierList.class, SIList.class, List.class);
    public static final AtrRef<STypeString, SIString, String>                     ATR_ANNOTATION_LABEL      = new AtrRef<>(SPackageBasic.class, "annotation_label"      , STypeString.class, SIString.class, String.class);
    public static final AtrRef<STypeBoolean, SIBoolean, Boolean>                  ATR_VISIBLE               = new AtrRef<>(SPackageBasic.class, "visible"               , STypeBoolean.class, SIBoolean.class, Boolean.class);
    public static final AtrRef<STypePredicate, SIPredicate, Predicate<SInstance>> ATR_VISIBLE_FUNCTION      = new AtrRef  (SPackageBasic.class, "visibleFunction"       , STypePredicate.class, SIPredicate.class, Predicate.class);
    public static final AtrRef<STypeBoolean, SIBoolean, Boolean>                  ATR_ENABLED               = new AtrRef<>(SPackageBasic.class, "enabled"               , STypeBoolean.class, SIBoolean.class, Boolean.class);
    public static final AtrRef<STypePredicate, SIPredicate, Predicate<SInstance>> ATR_ENABLED_FUNCTION      = new AtrRef  (SPackageBasic.class, "enabledFunction"       , STypePredicate.class, SIPredicate.class, Predicate.class);
    public static final AtrRef<STypeBoolean, SIBoolean, Boolean>                  ATR_REQUIRED              = new AtrRef<>(SPackageBasic.class, "required"              , STypeBoolean.class, SIBoolean.class, Boolean.class);
    public static final AtrRef<STypePredicate, SIPredicate, Predicate<SInstance>> ATR_REQUIRED_FUNCTION     = new AtrRef  (SPackageBasic.class, "requiredFunction"      , STypePredicate.class, SIPredicate.class, Predicate.class);
    public static final AtrRef<STypeBoolean, SIBoolean, Boolean>                  ATR_EXISTS                = new AtrRef<>(SPackageBasic.class, "exists"                , STypeBoolean.class, SIBoolean.class, Boolean.class);
    public static final AtrRef<STypePredicate, SIPredicate, Predicate<SInstance>> ATR_EXISTS_FUNCTION       = new AtrRef  (SPackageBasic.class, "existsFunction"        , STypePredicate.class, SIPredicate.class, Predicate.class);
    public static final AtrRef<STypePhraseBreak, SIPhraseBreak, PhraseBreak>      ATR_PHRASE_BREAK          = new AtrRef<>(SPackageBasic.class, "phraseBreak"           , STypePhraseBreak.class, SIPhraseBreak.class, PhraseBreak.class);
    public static final AtrRef<STypeString, SIString, String>                     ATR_ITEM_LABEL            = new AtrRef<>(SPackageBasic.class, "itemLabel"             , STypeString.class, SIString.class, String.class);
    public static final AtrRef<STypeConsumer, SIConsumer, IConsumer>              ATR_INIT_LISTENER         = new AtrRef<>(SPackageBasic.class, "initListener"          , STypeConsumer.class, SIConsumer.class, IConsumer.class);
    public static final AtrRef<STypeConsumer, SIConsumer, IConsumer>              ATR_LOAD_LISTENER         = new AtrRef<>(SPackageBasic.class, "loadListener"          , STypeConsumer.class, SIConsumer.class, IConsumer.class);
    public static final AtrRef<STypeConsumer, SIConsumer, IConsumer>              ATR_UPDATE_LISTENER       = new AtrRef<>(SPackageBasic.class, "updateListener"        , STypeConsumer.class, SIConsumer.class, IConsumer.class);
    public static final AtrRef<STypeInteger, SIInteger, Integer>                  ATR_MINIMUM_SIZE          = new AtrRef<>(SPackageBasic.class, "minimumSize"           , STypeInteger.class, SIInteger.class, Integer.class);
    public static final AtrRef<STypeInteger, SIInteger, Integer>                  ATR_MAXIMUM_SIZE          = new AtrRef<>(SPackageBasic.class, "maximumSize"           , STypeInteger.class, SIInteger.class, Integer.class);
    public static final AtrRef<STypeLong, SILong, Long>                           ATR_MAX_FILE_SIZE         = new AtrRef<>(SPackageBasic.class, "maxFileSize"           , STypeLong.class, SILong.class, Long.class);
    public static final AtrRef<STypeString, SIString, String>                     ATR_ALLOWED_FILE_TYPES    = new AtrRef<>(SPackageBasic.class, "allowedFileTypes"      , STypeString.class, SIString.class, String.class);
    public static final AtrRef<STypeBoolean, SIBoolean, Boolean>                  ATR_UPPER_CASE_TEXT       = new AtrRef<>(SPackageBasic.class, "uppperCaseText"        , STypeBoolean.class, SIBoolean.class, Boolean.class);
    public static final AtrRef<STypeDate, SIDate, Date>                           ATR_MAX_DATE              = new AtrRef<>(SPackageBasic.class, "maxDate"               , STypeDate.class, SIDate.class, Date.class);
    public static final AtrRef<STypeDate, SIDate, Date>                           ATR_MIN_DATE              = new AtrRef<>(SPackageBasic.class, "minDate"               , STypeDate.class, SIDate.class, Date.class);


    public static final AtrRef<STypeSupplier<Collection<SType<?>>>, SISupplier<Collection<SType<?>>>, Supplier<Collection<AtrBasic.DelayedDependsOnResolver>>>
            ATR_DEPENDS_ON_FUNCTION = new AtrRef(SPackageBasic.class, "dependsOnFunction", STypeSupplier.class, SISupplier.class, Supplier.class);

    //    public static final AtrRef<MTipoBehavior, MIBehavior, IBehavior<MInstancia>>   ATR_ONCHANGE_BEHAVIOR = new AtrRef(MPacoteBasic.class, "onchangeBehavior", MTipoBehavior.class, MIBehavior.class, IBehavior.class);
    //@formatter:on

    @Override
    protected void onLoadPackage(@Nonnull PackageBuilder pb) {

        pb.createType(STypeBehavior.class);
        pb.createType(STypeSupplier.class);
        pb.createType(STypeConsumer.class);
        pb.createType(STypePhraseBreak.class);
        pb.createType(STypeAnnotationClassifierList.class);

        pb.createAttributeIntoType(SType.class, ATR_DEFAULT_IF_NULL);
        pb.createAttributeIntoType(SType.class, ATR_REQUIRED).withDefaultValueIfNull(Boolean.FALSE);
        pb.createAttributeIntoType(SType.class, ATR_REQUIRED_FUNCTION);
        pb.createAttributeIntoType(SType.class, ATR_EXISTS).withDefaultValueIfNull(Boolean.TRUE);
        pb.createAttributeIntoType(SType.class, ATR_EXISTS_FUNCTION);
        pb.createAttributeIntoType(STypeSimple.class, ATR_FORMULA);
        pb.createAttributeIntoType(STypeString.class, ATR_TRIM).withDefaultValueIfNull(Boolean.TRUE);
        pb.createAttributeIntoType(STypeString.class, ATR_EMPTY_TO_NULL).withDefaultValueIfNull(Boolean.TRUE);
        pb.createAttributeIntoType(STypeList.class, ATR_PHRASE_BREAK).withDefaultValueIfNull(PhraseBreak.COMMA);
        pb.createAttributeIntoType(STypeList.class, ATR_ITEM_LABEL);
        pb.createAttributeIntoType(STypeList.class, ATR_MAXIMUM_SIZE);
        pb.createAttributeIntoType(STypeList.class, ATR_MINIMUM_SIZE);

        pb.createAttributeIntoType(STypeDate.class, ATR_MAX_DATE);
        pb.createAttributeIntoType(STypeDate.class, ATR_MIN_DATE);

        // Cria os tipos de atributos
        pb.createAttributeType(ATR_MAX_LENGTH);
        pb.createAttributeType(ATR_INTEGER_MAX_LENGTH);
        pb.createAttributeType(ATR_FRACTIONAL_MAX_LENGTH);
        pb.createAttributeType(ATR_MAX_FILE_SIZE);
        pb.createAttributeType(ATR_ALLOWED_FILE_TYPES);
        pb.createAttributeType(ATR_UPPER_CASE_TEXT);

        // Aplica os atributos ao tipos
        pb.createAttributeIntoType(SType.class, ATR_LABEL);
        pb.createAttributeIntoType(SType.class, ATR_ENABLE_HTML_IN_LABEL);
        pb.createAttributeIntoType(SType.class, ATR_SUBTITLE);
        pb.createAttributeIntoType(SType.class, ATR_BASIC_MASK);
        pb.createAttributeIntoType(SType.class, ATR_REGEX_MASK);
        pb.createAttributeIntoType(SType.class, ATR_VISIBLE);
        pb.createAttributeIntoType(SType.class, ATR_ENABLED);
        pb.createAttributeIntoType(SType.class, ATR_VISIBLE_FUNCTION);
        pb.createAttributeIntoType(SType.class, ATR_ENABLED_FUNCTION);
        pb.createAttributeIntoType(SType.class, ATR_DEPENDS_ON_FUNCTION);
        pb.createAttributeIntoType(SType.class, ATR_DISPLAY_ORDER);
        pb.createAttributeIntoType(SType.class, ATR_ANNOTATED);
        pb.createAttributeIntoType(SType.class, ATR_ANNOTATION_LABEL);
        pb.createAttributeIntoType(SType.class, ATR_INIT_LISTENER);
        pb.createAttributeIntoType(SType.class, ATR_LOAD_LISTENER);
        pb.createAttributeIntoType(SType.class, ATR_UPDATE_LISTENER);

        pb.createAttributeIntoType(SType.class, ATR_DISPLAY_STRING);
        pb.createAttributeIntoType(SType.class, ATR_HELP);
        pb.createAttributeIntoType(SType.class, ATR_INSTRUCTION);

        pb.addAttribute(STypeString.class, ATR_MAX_LENGTH, STypeString.DEFAULT_SIZE);
        pb.addAttribute(STypeString.class, ATR_UPPER_CASE_TEXT, Boolean.FALSE);

        pb.addAttribute(STypeInteger.class, ATR_MAX_LENGTH);

        pb.addAttribute(STypeLong.class, ATR_MAX_LENGTH);

        pb.addAttribute(STypeDecimal.class, ATR_INTEGER_MAX_LENGTH, 9);
        pb.addAttribute(STypeDecimal.class, ATR_FRACTIONAL_MAX_LENGTH, 2);

        pb.addAttribute(STypeAttachment.class, ATR_MAX_FILE_SIZE, 100L * 1024 * 1024); // 100MB
        pb.addAttribute(STypeAttachment.class, ATR_ALLOWED_FILE_TYPES);

        pb.getType(SType.class).asAtr().displayString(ctx -> ctx.instanceContext().toStringDisplayDefault());

        //TODO vinicius: modificar essa funcionalidade para ser ativada por SType ou por package
        //        pb.getType(SType.class).setAttributeCalculation(ATR_LABEL, ctx -> SFormUtil.generateUserFriendlyName(ctx.instance().getName()));

        // defina o meta dado do meta dado
        //@formatter:off
        pb.getAttribute(ATR_LABEL).asAtr().label("Label").maxLength(50);
        pb.getAttribute(ATR_ENABLE_HTML_IN_LABEL).withDefaultValueIfNull(Boolean.FALSE).asAtr().label("Hablitar HTML no label");
        pb.getAttribute(ATR_SUBTITLE).asAtr().label("Sub Título").maxLength(50);
        pb.getAttribute(ATR_BASIC_MASK).asAtr().label("Basic mask").maxLength(20);
        pb.getAttribute(ATR_MAX_LENGTH).asAtr().label("Tamanho Máximo").maxLength(4);
        pb.getAttribute(ATR_INTEGER_MAX_LENGTH).asAtr().label("Tamanho Máximo").maxLength(4);
        pb.getAttribute(ATR_FRACTIONAL_MAX_LENGTH).asAtr().label("Qtd. de Decimais").maxLength(4);
        pb.getAttribute(ATR_REQUIRED).asAtr().label("Obrigatório");
        pb.getAttribute(ATR_VISIBLE).asAtr().label("Visível");
        pb.getAttribute(ATR_VISIBLE_FUNCTION).asAtr().label("Visible (function)");
        pb.getAttribute(ATR_ENABLED).asAtr().label("Habilitado");
        pb.getAttribute(ATR_ENABLED_FUNCTION).asAtr().label("Enabled (function)");
        pb.getAttribute(ATR_DEPENDS_ON_FUNCTION).asAtr().label("Depends on (function)");
        pb.getAttribute(ATR_DISPLAY_ORDER).asAtr().label("Display order");
        pb.getAttribute(ATR_ITEM_LABEL).asAtr().label("Item label").maxLength(50);
        pb.getAttribute(ATR_HELP).asAtr().label("Texto Ajuda");
        pb.getAttribute(ATR_MINIMUM_SIZE).asAtr().label("Tamanho Mínimo Lista");
        pb.getAttribute(ATR_MAXIMUM_SIZE).asAtr().label("Tamanho Máximo Lista");
        pb.getAttribute(ATR_ALLOWED_FILE_TYPES).asAtr().label("Tipos de anexo permitido");
        //@formatter:on

    }
}
