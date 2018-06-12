package ladysnake.dissolution.unused.api;

public enum DistillateTypes implements INBTSerializableType<DistillateTypes> {

    METALLIS, CINNABARIS, SALIS, SULPURIS, UNTYPED;

    public static final INBTTypeSerializer<DistillateTypes> SERIALIZER = new EnumNBTTypeSerializer<>(DistillateTypes.class);

    @Override
    public INBTTypeSerializer<DistillateTypes> getSerializer() {
        return SERIALIZER;
    }

}
